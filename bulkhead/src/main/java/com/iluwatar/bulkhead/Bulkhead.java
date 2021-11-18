/*
 * The MIT License
 * Copyright © 2014-2021 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.iluwatar.bulkhead;

/**
 * A bulkhead can be used to decorate runnable and limit the number of parallel threads.
 */
public interface Bulkhead {

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1327
  /**
   * Decorates a runnable such that the thread resource used by a runnable is limited by
   * the bulkhead. If the number of concurrent threads has reached the bulkhead limit,
   * the runnable thread has to wait. If the waiting time is above the timeout value,
   * it should fail, without consuming the thread resource anymore.
   *
   * @param runnable the original Runnable.
   * @return a runnable which is decorated by a bulkhead.
   */
  Runnable decorate(final Runnable runnable);
}
