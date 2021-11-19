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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * A bulkhead implementation based on a semaphore.
 */
public class SemaphoreBulkhead implements Bulkhead {

  private final Semaphore semaphore;

  private final long timeout;

  /**
   * Creates a bulkhead with the max number of concurrent calls and timeout value.
   *
   * @param maxConcurrentCalls the max number of concurrent calls the bulkhead allows.
   * @param timeout the timeout value a call should wait.
   */
  public SemaphoreBulkhead(final int maxConcurrentCalls, final long timeout) {
    this.timeout = timeout;
    this.semaphore = new Semaphore(maxConcurrentCalls, true);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalThreadStateException when the bulkhead is full
   * @throws IllegalStateException if the thread is interrupted during waiting for permission
   */
  @Override
  public Runnable decorate(final Runnable runnable) {
    return () -> {
      acquirePermission();
      try {
        runnable.run();
      } finally {
        releasePermission();
      }
    };
  }

  private void acquirePermission() {
    try {
      boolean acquired = semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);
      if (!acquired) {
        throw new IllegalThreadStateException("Bulkhead full of threads");
      }
    } catch (final InterruptedException e) {
      throw new IllegalStateException("Bulkhead acquire permission cancelled", e);
    }
  }

  private void releasePermission() {
    semaphore.release();
  }
}