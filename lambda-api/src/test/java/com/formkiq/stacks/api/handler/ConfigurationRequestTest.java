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
package com.formkiq.stacks.api.handler;

import static com.formkiq.stacks.dynamodb.ConfigService.CHATGPT_API_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.formkiq.aws.dynamodb.DynamicObject;
import com.formkiq.client.invoker.ApiException;
import com.formkiq.client.model.GetConfigurationResponse;
import com.formkiq.client.model.SetConfigRequest;
import com.formkiq.client.model.SetConfigResponse;
import com.formkiq.stacks.dynamodb.ConfigService;
import com.formkiq.testutils.aws.DynamoDbExtension;
import com.formkiq.testutils.aws.LocalStackExtension;

/** Unit Tests for request /configuration. */
@ExtendWith(DynamoDbExtension.class)
@ExtendWith(LocalStackExtension.class)
public class ConfigurationRequestTest extends AbstractApiClientRequestTest {

  /**
   * Get /config default as Admin.
   *
   * @throws Exception an error has occurred
   */
  @Test
  public void testHandleGetConfiguration01() throws Exception {
    // given
    String siteId = null;
    String group = "Admins";

    ConfigService config = getAwsServices().getExtension(ConfigService.class);
    config.save(siteId, new DynamicObject(Map.of("chatGptApiKey", "somevalue")));

    setBearerToken(group);

    // when
    SetConfigResponse updateConfig =
        this.systemApi.updateConfig(new SetConfigRequest().chatGptApiKey("anothervalue"), siteId);
    GetConfigurationResponse response = this.systemApi.getConfigs(siteId);

    // then
    assertEquals("Config saved", updateConfig.getMessage());
    assertEquals("anot*******alue", response.getChatGptApiKey());
    assertEquals("", response.getMaxContentLengthBytes());
    assertEquals("", response.getMaxDocuments());
    assertEquals("", response.getMaxWebhooks());
    assertEquals("", response.getSmtpServer());
    assertEquals("", response.getSmtpUsername());
    assertEquals("", response.getSmtpPassword());
  }

  /**
   * Get /config default as User.
   *
   * @throws Exception an error has occurred
   */
  @Test
  public void testHandleGetConfiguration02() throws Exception {
    // given
    String siteId = null;
    ConfigService config = getAwsServices().getExtension(ConfigService.class);
    config.save(siteId, new DynamicObject(Map.of(CHATGPT_API_KEY, "somevalue")));

    String group = "Admins";
    setBearerToken(group);

    // when
    GetConfigurationResponse response = this.systemApi.getConfigs(siteId);

    // then
    assertEquals("some*******alue", response.getChatGptApiKey());
    assertEquals("", response.getMaxContentLengthBytes());
    assertEquals("", response.getMaxDocuments());
    assertEquals("", response.getMaxWebhooks());
    assertEquals("", response.getSmtpServer());
    assertEquals("", response.getSmtpUsername());
    assertEquals("", response.getSmtpPassword());
  }

  /**
   * Get /config for siteId, Config in default.
   *
   * @throws Exception an error has occurred
   */

  @Test
  public void testHandleGetConfiguration03() throws Exception {
    // given
    String siteId = UUID.randomUUID().toString();
    String group = "Admins";
    setBearerToken(group);

    ConfigService config = getAwsServices().getExtension(ConfigService.class);
    config.save(null, new DynamicObject(Map.of(CHATGPT_API_KEY, "somevalue")));

    // when
    GetConfigurationResponse response = this.systemApi.getConfigs(siteId);

    // then
    assertEquals("some*******alue", response.getChatGptApiKey());
    assertEquals("", response.getMaxContentLengthBytes());
    assertEquals("", response.getMaxDocuments());
    assertEquals("", response.getMaxWebhooks());
    assertEquals("", response.getSmtpServer());
    assertEquals("", response.getSmtpUsername());
    assertEquals("", response.getSmtpPassword());
  }

  /**
   * Get /config for siteId, Config in siteId.
   *
   * @throws Exception an error has occurred
   */
  @Test
  public void testHandleGetConfiguration04() throws Exception {
    // given
    String siteId = UUID.randomUUID().toString();
    String group = "Admins";
    setBearerToken(group);

    ConfigService config = getAwsServices().getExtension(ConfigService.class);
    config.save(null, new DynamicObject(Map.of(CHATGPT_API_KEY, "somevalue")));
    config.save(siteId, new DynamicObject(Map.of(CHATGPT_API_KEY, "anothervalue")));

    // when
    GetConfigurationResponse response = this.systemApi.getConfigs(siteId);

    // then
    assertEquals("anot*******alue", response.getChatGptApiKey());
    assertEquals("", response.getMaxContentLengthBytes());
    assertEquals("", response.getMaxDocuments());
    assertEquals("", response.getMaxWebhooks());
    assertEquals("", response.getSmtpServer());
    assertEquals("", response.getSmtpUsername());
    assertEquals("", response.getSmtpPassword());
  }

  /**
   * PUT /config default as Admin.
   *
   * @throws Exception an error has occurred
   */
  @Test
  public void testHandlePutConfiguration01() throws Exception {
    // given
    String siteId = null;
    String group = "Admins";
    setBearerToken(group);

    SetConfigRequest config = new SetConfigRequest().chatGptApiKey("anotherkey")
        .maxContentLengthBytes("1000000").maxDocuments("1000").maxWebhooks("5");

    // when
    SetConfigResponse configResponse = this.systemApi.updateConfig(config, siteId);
    GetConfigurationResponse response = this.systemApi.getConfigs(siteId);

    // then
    assertEquals("Config saved", configResponse.getMessage());

    assertEquals("anot*******rkey", response.getChatGptApiKey());
    assertEquals("1000000", response.getMaxContentLengthBytes());
    assertEquals("1000", response.getMaxDocuments());
    assertEquals("5", response.getMaxWebhooks());
    assertEquals("", response.getSmtpServer());
    assertEquals("", response.getSmtpUsername());
    assertEquals("", response.getSmtpPassword());
  }

  /**
   * PUT /config default as user.
   *
   * @throws Exception an error has occurred
   */
  @Test
  public void testHandlePutConfiguration02() throws Exception {
    // given
    String siteId = null;
    String group = "default";
    setBearerToken(group);

    SetConfigRequest config = new SetConfigRequest().chatGptApiKey("anotherkey")
        .maxContentLengthBytes("1000000").maxDocuments("1000").maxWebhooks("5");

    // when
    try {
      this.systemApi.updateConfig(config, siteId);
      fail();
    } catch (ApiException e) {
      final int code = 401;
      assertEquals(code, e.getCode());
    }
  }

  /**
   * PUT /config smtp.
   *
   * @throws Exception an error has occurred
   */
  @Test
  public void testHandlePutConfiguration03() throws Exception {
    // given
    String siteId = null;
    String group = "Admins";
    setBearerToken(group);

    SetConfigRequest config = new SetConfigRequest().smtpServer("server2").smtpUsername("username2")
        .smtpPassword("password2");

    // when
    SetConfigResponse configResponse = this.systemApi.updateConfig(config, siteId);
    GetConfigurationResponse response = this.systemApi.getConfigs(siteId);

    // then
    assertEquals("Config saved", configResponse.getMessage());

    assertEquals("server2", response.getSmtpServer());
    assertEquals("username2", response.getSmtpUsername());
    assertEquals("************", response.getSmtpPassword());
  }
}
