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
package com.formkiq.stacks.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.formkiq.aws.s3.S3ConnectionBuilder;
import com.formkiq.aws.s3.S3Service;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/** Unit Tests for {@link ConsoleInstallHandler}. */
public class ConsoleInstallHandlerTest {

  /** {@link ConsoleInstallHandler}. */
  private ConsoleInstallHandler handler;

  /** {@link LambdaContextRecorder}. */
  private LambdaContextRecorder context = new LambdaContextRecorder();
  /** {@link LambdaLogger}. */
  private LambdaLoggerRecorder logger = this.context.getLoggerRecorder();
  /** {@link HttpURLConnection}. */
  private static HttpUrlConnectionRecorder connection;
  /** {@link S3ConnectionBuilder}. */
  private static S3ConnectionBuilder s3Connection;
  /** {@link S3Service}. */
  private static S3Service s3;
  /** LocalStack {@link DockerImageName}. */
  private static DockerImageName localStackImage =
      DockerImageName.parse("localstack/localstack:0.12.2");
  /** {@link LocalStackContainer}. */
  private static LocalStackContainer localstack =
      new LocalStackContainer(localStackImage).withServices(Service.S3);

  /**
   * Before Class.
   * 
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   * @throws URISyntaxException URISyntaxException
   */
  @BeforeClass
  public static void beforeClass() throws IOException, URISyntaxException, InterruptedException {

    AwsCredentialsProvider cred = StaticCredentialsProvider
        .create(AwsSessionCredentials.create("ACCESSKEY", "SECRETKEY", "TOKENKEY"));

    localstack.start();

    s3Connection = new S3ConnectionBuilder().setCredentials(cred).setRegion(Region.US_EAST_1)
        .setEndpointOverride(localstack.getEndpointOverride(Service.S3).toString());

    s3 = new S3Service(s3Connection);

    try (S3Client s = s3.buildClient()) {

      s3.createBucket(s, "distrobucket");
      s3.createBucket(s, "destbucket");

      try (InputStream is = LambdaContextRecorder.class.getResourceAsStream("/test.zip")) {
        s3.putObject(s, "distrobucket", "formkiq-console/0.1/formkiq-console.zip", is, null);
      }
    }

    connection = new HttpUrlConnectionRecorder(new URL("http://localhost"));

  }

  /** before. */
  @Before
  public void before() {

    Map<String, String> map = new HashMap<>();
    map.put("CONSOLE_VERSION", "0.1");
    map.put("REGION", "us-east-1");
    map.put("DISTRIBUTION_BUCKET", "distrobucket");
    map.put("CONSOLE_BUCKET", "destbucket");
    map.put("API_URL",
        "https://chartapi.24hourcharts.com.execute-api.us-east-1.amazonaws.com/prod/");
    map.put("API_AUTH_URL", "https://auth.execute-api.us-east-1.amazonaws.com/prod/");
    map.put("API_WEBSOCKET_URL", "wss://me.execute-api.us-east-1.amazonaws.com/prod/");
    map.put("BRAND", "24hourcharts");
    map.put("ALLOW_ADMIN_CREATE_USER_ONLY", "false");
    map.put("COGNITO_HOSTED_UI", "https://test2622653865277.auth.us-east-2.amazoncognito.com");
    map.put("USER_AUTHENTICATION", "cognito");

    this.handler = new ConsoleInstallHandler(map, s3Connection, s3Connection) {

      @Override
      protected HttpURLConnection getConnection(final String responseUrl) throws IOException {
        return ConsoleInstallHandlerTest.this.getConnection();
      }
    };
  }

  /**
   * Create CloudFormation Input such as.
   *
   * @param requestType {@link String}
   * 
   *        <pre>
   * { "RequestType": "Create",
   * "ServiceToken": "arn:aws:lambda:...:function:route53Dependency",
   * "ResponseURL": "https://cloudformation-custom-resource", "StackId":
   * "arn:aws:cloudformation:eu-west-1:...", "RequestId":
   * "afd8d7c5-9376-4013-8b3b-307517b8719e", "LogicalResourceId": "Route53",
   * "ResourceType": "Custom::Route53Dependency", "ResourceProperties": {
   * "ServiceToken": "arn:aws:lambda:...:function:route53Dependency",
   * "DomainName": "example.com" } }
   *        </pre>
   *
   * @return {@link Map}
   */
  private Map<String, Object> createInput(final String requestType) {
    Map<String, Object> map = new HashMap<>();
    map.put("RequestType", requestType);
    map.put("ResponseURL", "https://cloudformation-custom-resource");
    return map;
  }

  /**
   * expect Send Response.
   *
   * @param contentLength int
   * @throws IOException IOException
   */
  private void verifySendResponse(final int contentLength) throws IOException {

    assertTrue(this.logger.containsString("Response Code: 200"));
    assertTrue(connection.getDoOutput());
    assertEquals("", connection.getRequestProperty("Content-Type"));
    assertEquals("" + contentLength, connection.getRequestProperty("Content-Length"));
    assertEquals("PUT", connection.getRequestMethod());
  }

  /**
   * Verify Config File is written.
   */
  private void verifyConfigWritten() {
    String config = String.format(
        "{%n\"url\": {%n\"cognitoHostedUi\":\"%s\",%n\"authApi\":\"%s\",%n\"chartApi\":\"%s\","
            + "%n\"webSocketApi\":\"%s\",%n\"documentApi\":\"%s\"}"
            + ",\"consoleversion\":\"%s\",\"brand\":\"%s\",\"allowAdminCreateUserOnly\":\"%s\","
            + "\"userAuthentication\":\"cognito\"}",
        "https://test2622653865277.auth.us-east-2.amazoncognito.com",
        "https://auth.execute-api.us-east-1.amazonaws.com/prod/",
        "https://chartapi.24hourcharts.com", "wss://me.execute-api.us-east-1.amazonaws.com/prod/",
        "https://chartapi.24hourcharts.com.execute-api.us-east-1.amazonaws.com/prod/", "0.1",
        "24hourcharts", "false");

    assertTrue(this.logger.containsString("writing Cognito config: " + config));
  }

  /**
   * Get Connection.
   *
   * @return {@link HttpURLConnection}
   */
  protected HttpURLConnection getConnection() {
    return connection;
  }

  /**
   * Test Handle Request 'CREATE'.
   *
   * @throws Exception Exception
   */
  @Test
  public void testHandleRequest01() throws Exception {
    // given
    final int contentlength = 105;
    final Map<String, Object> input = createInput("Create");

    // when
    this.logger.log(
        "received input: {ResponseURL=https://cloudformation-custom-resource, RequestType=Create}");
    this.logger.log("unpacking formkiq-console/0.1/formkiq-console.zip "
        + "from bucket distrobucket to bucket destbucket");
    this.logger.log("sending SUCCESS to https://cloudformation-custom-resource");
    this.logger.log("Request Create was successful!");

    input.put("CONSOLE_BUCKET", "destbucket");

    // replayAll();
    this.handler.handleRequest(input, this.context);

    // then
    verifySendResponse(contentlength);
    verifyConfigWritten();

    assertTrue(connection.contains("\"Status\":\"SUCCESS\""));
    assertTrue(connection.contains("\"Data\":{\"Message\":\"Request Create was successful!\""));

    try (S3Client s = s3.buildClient()) {
      assertEquals("font/woff2",
          s3.getObjectMetadata(s, "destbucket", "0.1/font.woff2").getContentType());

      assertEquals("text/css",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.css").getContentType());

      assertEquals("application/vnd.ms-fontobject",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.eot").getContentType());

      assertEquals("image/x-icon",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.ico").getContentType());

      assertEquals("application/javascript",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.js").getContentType());

      assertEquals("image/svg+xml",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.svg").getContentType());

      assertEquals("font/ttf",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.ttf").getContentType());

      assertEquals("text/plain",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.txt").getContentType());

      assertEquals("font/woff",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.woff").getContentType());
    }
  }

  /**
   * Test Handle Request 'UPDATE'.
   *
   * @throws Exception Exception
   */
  @Test
  public void testHandleRequest02() throws Exception {
    // given
    final int contentlength = 105;
    final Map<String, Object> input = createInput("Update");

    // when
    this.handler.handleRequest(input, this.context);

    // then
    verifySendResponse(contentlength);
    verifyConfigWritten();

    assertTrue(this.logger.containsString(
        "received input: {ResponseURL=https://cloudformation-custom-resource, RequestType=Update}"));

    assertTrue(this.logger.containsString("unpacking formkiq-console/0.1/formkiq-console.zip "
        + "from bucket distrobucket to bucket destbucket"));
    assertTrue(
        this.logger.containsString("sending SUCCESS to https://cloudformation-custom-resource"));
    assertTrue(this.logger.containsString("Request Update was successful!"));

    assertTrue(connection.contains("\"Status\":\"SUCCESS\""));
    assertTrue(connection.contains("\"Data\":{\"Message\":\"Request Update was successful!\""));

    try (S3Client s = s3.buildClient()) {
      assertEquals("font/woff2",
          s3.getObjectMetadata(s, "destbucket", "0.1/font.woff2").getContentType());

      assertEquals("text/css",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.css").getContentType());

      assertEquals("application/vnd.ms-fontobject",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.eot").getContentType());

      assertEquals("image/x-icon",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.ico").getContentType());

      assertEquals("application/javascript",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.js").getContentType());

      assertEquals("image/svg+xml",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.svg").getContentType());

      assertEquals("font/ttf",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.ttf").getContentType());

      assertEquals("text/plain",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.txt").getContentType());

      assertEquals("font/woff",
          s3.getObjectMetadata(s, "destbucket", "0.1/test.woff").getContentType());
    }
  }

  /**
   * Test Handle Request 'DELETE'.
   *
   * @throws Exception Exception
   */
  @Test
  public void testHandleRequest03() throws Exception {
    // given
    final int contentlength = 105;
    final Map<String, Object> input = createInput("Delete");

    // when
    this.handler.handleRequest(input, this.context);

    // then
    verifySendResponse(contentlength);

    assertTrue(this.logger.containsString(
        "received input: {ResponseURL=https://cloudformation-custom-resource, RequestType=Delete}"));

    assertTrue(this.logger.containsString("Request Delete was successful!"));
    assertTrue(
        this.logger.containsString("sending SUCCESS to https://cloudformation-custom-resource"));

    assertTrue(connection.contains("\"Status\":\"SUCCESS\""));
    assertTrue(connection.contains("\"Data\":{\"Message\":\"Request Delete was successful!\""));
  }

  /**
   * Test Handle Request 'UNKNOWN'.
   *
   * @throws Exception Exception
   */
  @Test
  public void testHandleRequest04() throws Exception {
    // given
    final int contentlength = 122;
    final Map<String, Object> input = createInput("UNKNOWN");

    // when
    this.handler.handleRequest(input, this.context);

    // then
    verifySendResponse(contentlength);
    assertTrue(this.logger
        .containsString("received input: {ResponseURL=https://cloudformation-custom-resource, "
            + "RequestType=UNKNOWN}"));

    assertTrue(this.logger.containsString("received RequestType UNKNOWN skipping unpacking"));
    assertTrue(
        this.logger.containsString("sending FAILURE to https://cloudformation-custom-resource"));

    assertTrue(connection.contains("\"Status\":\"FAILURE\""));
  }
}
