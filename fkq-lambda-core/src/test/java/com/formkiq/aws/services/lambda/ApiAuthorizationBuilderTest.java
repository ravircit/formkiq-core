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
package com.formkiq.aws.services.lambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * 
 * Unit Test for {@link ApiAuthorizer}.
 *
 */
class ApiAuthorizationBuilderTest {

  /**
   * Get {@link ApiGatewayRequestEvent}.
   * 
   * @param group {@link String}
   * @return {@link ApiGatewayRequestEvent}
   */
  private ApiGatewayRequestEvent getJwtEvent(final String group) {
    ApiGatewayRequestEvent event = new ApiGatewayRequestEvent();
    ApiGatewayRequestContext content = new ApiGatewayRequestContext();
    content.setAuthorizer(Map.of("claims", Map.of("cognito:groups", group)));
    event.setRequestContext(content);
    return event;
  }

  /**
   * Get {@link ApiGatewayRequestEvent}.
   * 
   * @param userArn {@link String}
   * @return {@link ApiGatewayRequestEvent}
   */
  private ApiGatewayRequestEvent getUserArnEvent(final String userArn) {
    ApiGatewayRequestEvent event = new ApiGatewayRequestEvent();
    ApiGatewayRequestContext content = new ApiGatewayRequestContext();
    content.setIdentity(Map.of("userArn", userArn));
    event.setRequestContext(content);
    return event;
  }

  /**
   * Basic 'default'/SiteId access.
   */
  @Test
  void testApiAuthorizer01() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {

      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_default]" : "[default]";
      String s1 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_finance]" : "[finance]";

      ApiGatewayRequestEvent event0 = getJwtEvent(s0);
      ApiGatewayRequestEvent event1 = getJwtEvent(s1);

      // when
      final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);
      final ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

      // then
      assertEquals("default", api0.siteId());
      assertEquals("default", String.join(",", api0.siteIds()));
      assertEquals("READ,WRITE,DELETE",
          api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ,WRITE,DELETE)", api0.accessSummary());

      assertEquals("finance", api1.siteId());
      assertEquals("finance", String.join(",", api1.siteIds()));
      assertEquals("READ,WRITE,DELETE",
          api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: finance (READ,WRITE,DELETE)", api1.accessSummary());
    }
  }

  /**
   * Basic 'default_read' access.
   */
  @Test
  void testApiAuthorizer02() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {
      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_default_read]" : "[default_read]";
      String s1 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_finance_read]" : "[finance_read]";
      ApiGatewayRequestEvent event0 = getJwtEvent(s0);
      ApiGatewayRequestEvent event1 = getJwtEvent(s1);

      // when
      final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);
      final ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

      // then
      assertEquals("default", api0.siteId());
      assertEquals("default", String.join(",", api0.siteIds()));
      assertEquals("READ",
          api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ)", api0.accessSummary());

      assertEquals("finance", api1.siteId());
      assertEquals("finance", String.join(",", api1.siteIds()));
      assertEquals("READ",
          api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: finance (READ)", api1.accessSummary());
    }
  }

  /**
   * Basic 'Admin' access.
   */
  @Test
  void testApiAuthorizer03() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {
      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_Admins formkiq_default]"
          : "[Admins default]";
      String s1 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_Admins formkiq_finance"
          : "[Admins finance]";
      ApiGatewayRequestEvent event0 = getJwtEvent(s0);
      ApiGatewayRequestEvent event1 = getJwtEvent(s1);

      // when
      final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);
      final ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

      // then
      assertEquals("default", api0.siteId());
      assertEquals("default", String.join(",", api0.siteIds()));
      assertEquals("READ,WRITE,DELETE,ADMIN",
          api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ,WRITE,DELETE,ADMIN)", api0.accessSummary());

      assertEquals("finance", api1.siteId());
      assertEquals("finance", String.join(",", api1.siteIds()));
      assertEquals("READ,WRITE,DELETE,ADMIN",
          api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: finance (READ,WRITE,DELETE,ADMIN)", api1.accessSummary());
    }
  }

  /**
   * Basic 'admin' access.
   */
  @Test
  void testApiAuthorizer04() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {
      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_admins formkiq_default]"
          : "[admins default]";
      String s1 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_admins formkiq_finance"
          : "[admins finance]";
      ApiGatewayRequestEvent event0 = getJwtEvent(s0);
      ApiGatewayRequestEvent event1 = getJwtEvent(s1);

      // when
      final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);
      final ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

      // then
      assertEquals("default", api0.siteId());
      assertEquals("default", String.join(",", api0.siteIds()));
      assertEquals("READ,WRITE,DELETE,ADMIN",
          api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ,WRITE,DELETE,ADMIN)", api0.accessSummary());

      assertEquals("finance", api1.siteId());
      assertEquals("finance", String.join(",", api1.siteIds()));
      assertEquals("READ,WRITE,DELETE,ADMIN",
          api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: finance (READ,WRITE,DELETE,ADMIN)", api1.accessSummary());
    }
  }

  /**
   * Multiple SiteId access with 'siteId' query.
   */
  @Test
  void testApiAuthorizer05() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {

      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_default formkiq_other]"
          : "[default other]";
      String s1 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_finance formkiq_other]"
          : "[finance other]";

      ApiGatewayRequestEvent event0 = getJwtEvent(s0);
      event0.setQueryStringParameters(Map.of("siteId", "other"));

      ApiGatewayRequestEvent event1 = getJwtEvent(s1);
      event1.setQueryStringParameters(Map.of("siteId", "other"));

      // when
      final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);
      final ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

      // then
      assertEquals("other", api0.siteId());
      assertEquals("default,other", String.join(",", api0.siteIds()));
      assertEquals("READ,WRITE,DELETE",
          api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ,WRITE,DELETE), other (READ,WRITE,DELETE)",
          api0.accessSummary());

      assertEquals("other", api1.siteId());
      assertEquals("finance,other", String.join(",", api1.siteIds()));
      assertEquals("READ,WRITE,DELETE",
          api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: finance (READ,WRITE,DELETE), other (READ,WRITE,DELETE)",
          api1.accessSummary());
    }
  }

  /**
   * Multiple SiteId access without 'siteId' query.
   */
  @Test
  void testApiAuthorizer06() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {

      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_default formkiq_other]"
          : "[default other]";

      ApiGatewayRequestEvent event = getJwtEvent(s0);

      // when
      ApiAuthorization api = new ApiAuthorizationBuilder(type).build(event);

      // then
      assertNull(api.siteId());
      assertEquals("default,other", String.join(",", api.siteIds()));
      assertEquals("",
          api.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("READ,WRITE,DELETE",
          api.permissions("default").stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("READ,WRITE,DELETE",
          api.permissions("other").stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ,WRITE,DELETE), other (READ,WRITE,DELETE)",
          api.accessSummary());
    }
  }

  /**
   * UserArn assume-role / user:.
   */
  @Test
  void testApiAuthorizer07() throws Exception {
    // given
    String userArn0 = "arn:aws:sts::111111111111:assumed-role/formkiqIUK/ApiGatewayInvokeRole";
    String userArn1 = "arn:aws:iam::1111111111111111:user/" + UUID.randomUUID();

    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {

      for (String userArn : Arrays.asList(userArn0, userArn1)) {

        ApiGatewayRequestEvent event0 = getUserArnEvent(userArn);

        // when
        ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);

        // then
        assertEquals("default", api0.siteId());
        assertEquals("default", String.join(",", api0.siteIds()));
        assertEquals("READ,WRITE,DELETE,ADMIN",
            api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
        assertEquals("groups: default (READ,WRITE,DELETE,ADMIN)", api0.accessSummary());

        // given
        ApiGatewayRequestEvent event1 = getUserArnEvent(userArn);
        event1.setQueryStringParameters(Map.of("siteId", "finance"));

        // when
        ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

        // then
        assertEquals("finance", api1.siteId());
        assertEquals("finance", String.join(",", api1.siteIds()));
        assertEquals("READ,WRITE,DELETE,ADMIN",
            api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
        assertEquals("groups: finance (READ,WRITE,DELETE,ADMIN)", api1.accessSummary());
      }
    }
  }

  /**
   * Invalid UserArn.
   */
  @Test
  void testApiAuthorizer08() throws Exception {
    // given
    String userArn = "arn:aws:sts::111111111111:another/formkiqIUK/ApiGatewayInvokeRole";

    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {

      ApiGatewayRequestEvent event = getUserArnEvent(userArn);

      // when
      ApiAuthorization api = new ApiAuthorizationBuilder(type).build(event);

      // then
      assertNull(api.siteId());
      assertEquals("", String.join(",", api.siteIds()));
      assertEquals("",
          api.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("no groups", api.accessSummary());
    }
  }

  /**
   * SAML - multiple groups.
   */
  @Test
  void testApiAuthorizer09() throws Exception {
    // given
    ApiAuthorizerType type = ApiAuthorizerType.SAML;

    String s = "[formkiq_finance other]";

    ApiGatewayRequestEvent event = getJwtEvent(s);
    event.setQueryStringParameters(Map.of("siteId", "finance"));

    // when
    final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event);

    // then
    assertEquals("finance", api0.siteId());
    assertEquals("finance", String.join(",", api0.siteIds()));
    assertEquals("READ,WRITE,DELETE",
        api0.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
    assertEquals("groups: finance (READ,WRITE,DELETE)", api0.accessSummary());
  }

  /**
   * User has 'default' & 'default_read'.
   */
  @Test
  void testApiAuthorizer10() throws Exception {
    // given
    for (ApiAuthorizerType type : Arrays.asList(ApiAuthorizerType.COGNITO,
        ApiAuthorizerType.SAML)) {

      String s0 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_default formkiq_default_read]"
          : "[default default_read]";
      String s1 = ApiAuthorizerType.SAML.equals(type) ? "[formkiq_finance formkiq_finance_read]"
          : "[finance finance_read]";

      ApiGatewayRequestEvent event0 = getJwtEvent(s0);
      ApiGatewayRequestEvent event1 = getJwtEvent(s1);

      // when
      final ApiAuthorization api0 = new ApiAuthorizationBuilder(type).build(event0);
      final ApiAuthorization api1 = new ApiAuthorizationBuilder(type).build(event1);

      // then
      assertEquals("default", api0.siteId());
      assertEquals("default", String.join(",", api0.siteIds()));
      assertEquals("DELETE,READ,WRITE", api0.permissions().stream().map(p -> p.name())
          .sorted(String::compareTo).collect(Collectors.joining(",")));
      assertEquals("groups: default (READ,WRITE,DELETE)", api0.accessSummary());

      assertEquals("finance", api1.siteId());
      assertEquals("finance", String.join(",", api1.siteIds()));
      assertEquals("READ,WRITE,DELETE",
          api1.permissions().stream().map(p -> p.name()).collect(Collectors.joining(",")));
      assertEquals("groups: finance (READ,WRITE,DELETE)", api1.accessSummary());
    }
  }

}
