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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;

/**
 * Http Method 'Options' {@link HttpRequestHandler}.
 */
public class OptionsHttpRequestHandler implements HttpRequestHandler {

  @Override
  public void handle(final ChannelHandlerContext ctx, final FullHttpRequest request) {
    HttpHeaders headers = new DefaultHttpHeaders();
    headers.add("Access-Control-Allow-Headers", "*");
    headers.add("Access-Control-Allow-Methods", "*");
    headers.add("Access-Control-Allow-Origin", "*");
    headers.add("Content-Type", "application/json");

    DefaultFullHttpResponse response = buildResponse(HttpResponseStatus.OK, "ok");
    HttpUtil.setContentLength(response, response.content().readableBytes());

    response.headers().set(headers);

    ctx.writeAndFlush(response);
  }

  @Override
  public boolean isSupported(final FullHttpRequest request) {
    return request.method().equals(HttpMethod.OPTIONS);
  }

}
