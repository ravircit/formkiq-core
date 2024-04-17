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

//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//import com.formkiq.lambda.runtime.graalvm.LambdaContext;
import com.formkiq.module.lambdaservices.AwsServiceCache;

import java.util.List;
import java.util.Optional;
//import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Implementation that supports multiple {@link AuthzAuthorizationHandlers}. If any
 * {@link AuthzAuthorizationHandlers} is not authorized, they will all fail.
 *
 */
public class AuthzAuthorizationHandlers implements AuthorizationHandler {


  /** {@link List} {@link AuthorizationHandler}. */
  private List<AuthorizationHandler> handlers;

//  /** log. */
//  Context context = new LambdaContext(UUID.randomUUID().toString());

  /** log. */
  static Logger logger = Logger.getLogger(AuthzAuthorizationHandlers.class.getName());

  /**
   * constructor.
   *
   * @param authorizationHandlers {@link List} {@link AuthzAuthorizationHandlers}
   */
  public AuthzAuthorizationHandlers(final List<AuthorizationHandler> authorizationHandlers) {
    this.handlers = authorizationHandlers;
  }

  @Override
  public Optional<Boolean> isAuthorized(final AwsServiceCache awsServices,
                                        final ApiGatewayRequestEvent event, final ApiAuthorization authorization) {
    logger.log(Level.INFO, event.getBody());
//    LambdaLogger logger1 = context.getLogger();
//    logger1.log("cloudwatch AuthzAuthorizationHandlers formkiqtest: " + event.getBody());


    return null;
  }
}