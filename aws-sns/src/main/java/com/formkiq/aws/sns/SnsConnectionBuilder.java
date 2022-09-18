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
package com.formkiq.aws.sns;

import java.net.URI;
import java.net.URISyntaxException;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;

/**
 * 
 * Sns Connection Builder.
 *
 */
public class SnsConnectionBuilder {

  /** {@link SnsClientBuilder}. */
  private SnsClientBuilder builder;
  /** {@link SnsClient}. */
  private SnsClient snsClient = null;

  /**
   * constructor.
   */
  public SnsConnectionBuilder() {
    System.setProperty("software.amazon.awssdk.http.service.impl",
        "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService");

    this.builder = SnsClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder())
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create());
  }

  /**
   * Build {@link SnsClient}.
   * 
   * @return {@link SnsClient}
   */
  public SnsClient build() {
    if (this.snsClient == null) {
      this.snsClient = this.builder.build();
    }

    return this.snsClient;
  }

  /**
   * Set Credentials.
   * 
   * @param cred {@link AwsCredentialsProvider}
   * @return {@link SnsConnectionBuilder}
   */
  public SnsConnectionBuilder setCredentials(final AwsCredentialsProvider cred) {
    this.builder = this.builder.credentialsProvider(cred);
    return this;
  }

  /**
   * Set Credentials.
   * 
   * @param credentials {@link String}
   * @return {@link SnsConnectionBuilder}
   */
  public SnsConnectionBuilder setCredentials(final String credentials) {
    try (ProfileCredentialsProvider prov =
        ProfileCredentialsProvider.builder().profileName(credentials).build()) {
      return setCredentials(prov);
    }
  }

  /**
   * Set Endpoint Override.
   * 
   * @param uri {@link String}
   * @return {@link SnsConnectionBuilder}
   * @throws URISyntaxException URISyntaxException
   */
  public SnsConnectionBuilder setEndpointOverride(final String uri) throws URISyntaxException {
    this.builder = this.builder.endpointOverride(new URI(uri));
    return this;
  }

  /**
   * Set Region.
   * 
   * @param region {@link Region}
   * @return {@link SnsConnectionBuilder}
   */
  public SnsConnectionBuilder setRegion(final Region region) {
    this.builder = this.builder.region(region);
    return this;
  }
}
