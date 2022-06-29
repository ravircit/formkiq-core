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
package com.formkiq.aws.dynamodb.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 
 * Objects Helper.
 *
 */
public class Objects {

  /**
   * Returns a {@link Collection} that is guarantee not to be null.
   * 
   * @param <T> Type
   * @param list {@link List}
   * @return {@link List}
   */
  public static <T> Collection<T> notNull(final Collection<T> list) {
    return list != null ? list : Collections.emptyList();
  }

  /**
   * Returns a {@link List} that is guarantee not to be null.
   * 
   * @param <T> Type
   * @param list {@link List}
   * @return {@link List}
   */
  public static <T> List<T> notNull(final List<T> list) {
    return list != null ? list : Collections.emptyList();
  }

  /**
   * Returns a {@link List} that is guarantee not to be null.
   * 
   * @param <T> Type
   * @param <S> Type
   * @param map {@link List}
   * @return {@link List}
   */
  public static <T, S> Map<T, S> notNull(final Map<T, S> map) {
    return map != null ? map : Collections.emptyMap();
  }
}