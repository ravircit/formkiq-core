/**
 * MIT License
 * 
 * Copyright (c) 2018 - 2020 FormKiQ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.formkiq.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import com.formkiq.aws.dynamodb.DynamoDbAwsServiceRegistry;
import com.formkiq.aws.dynamodb.DynamoDbConnectionBuilder;
import com.formkiq.aws.dynamodb.schema.DocumentSchema;
import com.formkiq.aws.s3.S3AwsServiceRegistry;
import com.formkiq.aws.sns.SnsAwsServiceRegistry;
import com.formkiq.aws.sns.SnsService;
import com.formkiq.aws.sqs.SqsAwsServiceRegistry;
import com.formkiq.aws.sqs.SqsService;
import com.formkiq.aws.ssm.SmsAwsServiceRegistry;
import com.formkiq.aws.ssm.SsmService;
import com.formkiq.module.lambdaservices.AwsServiceCache;
import com.formkiq.module.lambdaservices.AwsServiceCacheBuilder;
import com.formkiq.stacks.dynamodb.DocumentVersionServiceNoVersioning;
import com.formkiq.stacks.lambda.s3.DocumentsS3Update;
import com.formkiq.stacks.lambda.s3.StagingS3Create;
import io.minio.BucketExistsArgs;
import io.minio.GetBucketNotificationArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketNotificationArgs;
import io.minio.SetBucketVersioningArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.EventType;
import io.minio.messages.NotificationConfiguration;
import io.minio.messages.QueueConfiguration;
import io.minio.messages.VersioningConfiguration;
import io.minio.messages.VersioningConfiguration.Status;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * {@link ChannelInitializer} for Http Server.
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

  /** Cache Table. */
  private static final String CACHE_TABLE = "Cache";
  /** Document Syncs Table Name. */
  public static final String DOCUMENT_SYNCS_TABLE = "DocumentSyncs";
  /** Documents S3 Bucket. */
  private static final String DOCUMENTS_BUCKET = "documents";
  /** Documents Table. */
  private static final String DOCUMENTS_TABLE = "Documents";
  /** Max Content Length. */
  private static final int MAX_CONTENT_LENGTH = 5242880;
  /** Documents Stating S3 Bucket. */
  private static final String STAGING_DOCUMENTS_BUCKET = "stagingdocuments";
  /** {@link NettyRequestHandler}. */
  private NettyRequestHandler handler;
  /** {@link StagingS3Create}. */
  private StagingS3Create s3Create;
  /** {@link DocumentsS3Update}. */
  private DocumentsS3Update s3Update;

  /**
   * constructor.
   * 
   * @param commandLine {@link CommandLine}
   */
  public HttpServerInitializer(final CommandLine commandLine) {

    String minioAccessKey = commandLine.getOptionValue("minioAccessKey");
    String minioSecretKey = commandLine.getOptionValue("minioSecretKey");

    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider
        .create(AwsBasicCredentials.create(minioAccessKey, minioSecretKey));

    setupHandler(commandLine, credentialsProvider);
    setupS3Lambda(commandLine, credentialsProvider);
  }

  /**
   * Add S3 Notification.
   * 
   * @param mc {@link MinioClient}
   * @param bucket {@link String}
   * @param eventKey {@link String}
   * @throws IOException IOException
   * @throws IllegalArgumentException IllegalArgumentException
   * @throws XmlParserException XmlParserException
   * @throws ServerException ServerException
   * @throws NoSuchAlgorithmException NoSuchAlgorithmException
   * @throws InvalidResponseException InvalidResponseException
   * @throws InternalException InternalException
   * @throws InsufficientDataException InsufficientDataException
   * @throws ErrorResponseException ErrorResponseException
   * @throws InvalidKeyException InvalidKeyException
   */
  private void addEventNotification(final MinioClient mc, final String bucket,
      final String eventKey)
      throws InvalidKeyException, ErrorResponseException, InsufficientDataException,
      InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException,
      XmlParserException, IllegalArgumentException, IOException {

    NotificationConfiguration bucketNotification =
        mc.getBucketNotification(GetBucketNotificationArgs.builder().bucket(bucket).build());

    if (bucketNotification.queueConfigurationList().size() < 1) {
      NotificationConfiguration notificationConfiguration = new NotificationConfiguration();
      QueueConfiguration q = new QueueConfiguration();
      q.setEvents(Arrays.asList(EventType.OBJECT_CREATED_ANY));
      q.setQueue("arn:minio:sqs::" + eventKey + ":webhook");
      notificationConfiguration.setQueueConfigurationList(Arrays.asList(q));

      mc.setBucketNotification(SetBucketNotificationArgs.builder().bucket(bucket)
          .config(notificationConfiguration).build());
    }
  }

  /**
   * Create Minio S3 Buckets.
   * 
   * @param minioAccessKey {@link String}
   * @param minioSecretKey {@link String}
   * @param s3Url {@link String}
   * @throws IOException IOException
   */
  private void createS3Buckets(final String minioAccessKey, final String minioSecretKey,
      final String s3Url) throws IOException {

    MinioClient mc =
        MinioClient.builder().endpoint(s3Url).credentials(minioAccessKey, minioSecretKey).build();

    try {

      makeBucket(mc, DOCUMENTS_BUCKET);
      makeBucket(mc, STAGING_DOCUMENTS_BUCKET);

      addEventNotification(mc, DOCUMENTS_BUCKET, "DOCUMENTS");
      addEventNotification(mc, STAGING_DOCUMENTS_BUCKET, "STAGINGDOCUMENTS");

    } catch (InvalidKeyException | ErrorResponseException | InsufficientDataException
        | InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
        | XmlParserException | IllegalArgumentException | IOException e) {
      throw new IOException(e);
    }
  }

  private HttpServerHandler createServerHandler() {
    return new HttpServerHandler(this.handler, this.s3Create, this.s3Update);
  }

  private Map<String, URI> getEndpoints(final CommandLine commandLine) {

    String dynamoDbUrl = commandLine.getOptionValue("dynamodb-url");
    String s3Url = commandLine.getOptionValue("s3-url");

    try {
      Map<String, URI> awsServiceEndpoints =
          Map.of("dynamodb", new URI(dynamoDbUrl), "s3", new URI(s3Url));
      return awsServiceEndpoints;
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> getEnvironment() {
    Map<String, String> env = new HashMap<>();
    env.put("USER_AUTHENTICATION", "cognito");
    env.put("VERSION", "1.13");
    env.put("FORMKIQ_TYPE", "core");
    env.put("MODULE_fulltext", "true");
    env.put("MODULE_ocr", "true");
    env.put("DOCUMENTS_TABLE", DOCUMENTS_TABLE);
    env.put("CACHE_TABLE", CACHE_TABLE);
    env.put("DOCUMENTS_S3_BUCKET", DOCUMENTS_BUCKET);
    env.put("STAGE_DOCUMENTS_S3_BUCKET", STAGING_DOCUMENTS_BUCKET);
    env.put("AWS_REGION", "us-east-1");
    env.put("DOCUMENT_SYNC_TABLE", DOCUMENT_SYNCS_TABLE);
    env.put("SNS_DOCUMENT_EVENT", "");
    env.put("DOCUMENT_VERSIONS_PLUGIN", DocumentVersionServiceNoVersioning.class.getName());
    return env;
  }

  @Override
  public void initChannel(final SocketChannel ch) {
    ch.pipeline().addLast(new HttpServerCodec());
    ch.pipeline().addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
    ch.pipeline().addLast(new ChunkedWriteHandler());
    ch.pipeline().addLast(new HttpServerExpectContinueHandler());
    ch.pipeline().addLast(createServerHandler());
  }

  private void makeBucket(final MinioClient mc, final String bucket) throws ErrorResponseException,
      InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException,
      IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
    if (!mc.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
      mc.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
      mc.setBucketVersioning(SetBucketVersioningArgs.builder().bucket(bucket)
          .config(new VersioningConfiguration(Status.ENABLED, Boolean.FALSE)).build());
    }
  }

  /**
   * Setup FormKiQ.
   * 
   * @param commandLine {@link CommandLine}
   * @param credentialsProvider {@link AwsCredentialsProvider}
   */
  private void setupHandler(final CommandLine commandLine,
      final AwsCredentialsProvider credentialsProvider) {

    String minioAccessKey = commandLine.getOptionValue("minioAccessKey");
    String minioSecretKey = commandLine.getOptionValue("minioSecretKey");
    String s3Url = commandLine.getOptionValue("s3-url");

    Map<String, String> env = getEnvironment();

    try {

      Map<String, URI> awsServiceEndpoints = getEndpoints(commandLine);

      this.handler = new NettyRequestHandler(env, awsServiceEndpoints, credentialsProvider);

      AwsServiceCache aws = this.handler.getAwsServices();
      aws.deregister(SsmService.class);
      aws.deregister(SqsService.class);
      aws.deregister(SnsService.class);

      DynamoDbConnectionBuilder db = aws.getExtension(DynamoDbConnectionBuilder.class);

      try (DynamoDbClient dbClient = db.build()) {
        DocumentSchema schema = new DocumentSchema(dbClient);
        schema.createDocumentsTable(DOCUMENTS_TABLE);
        schema.createCacheTable(CACHE_TABLE);
      }

      createS3Buckets(minioAccessKey, minioSecretKey, s3Url);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Set S3 Lambda.
   * 
   * @param command {@link CommandLine}
   * @param credentialsProvider {@link AwsCredentialsProvider}
   */
  private void setupS3Lambda(final CommandLine command,
      final AwsCredentialsProvider credentialsProvider) {

    Map<String, String> env = getEnvironment();
    Map<String, URI> endpoints = getEndpoints(command);

    AwsServiceCache serviceCache = new AwsServiceCacheBuilder(env, endpoints, credentialsProvider)
        .addService(new DynamoDbAwsServiceRegistry(), new S3AwsServiceRegistry(),
            new SnsAwsServiceRegistry(), new SqsAwsServiceRegistry(), new SmsAwsServiceRegistry())
        .build();

    this.s3Create = new StagingS3Create(serviceCache);
    this.s3Update = new DocumentsS3Update(serviceCache);
  }
}