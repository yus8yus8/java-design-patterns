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

import lombok.extern.slf4j.Slf4j;

/**
 * The Bulkhead pattern is a type of application design that is tolerant of failure. In a bulkhead
 * architecture, elements of an application are isolated into pools so that if one fails, it will
 * not exhaust resources of other applications.
 *
 * <p>In the below example, it uses a bulkhead to limit the thread resources used by a remote
 * service call. The number of the maximum concurrent calls is set to 3, and the waiting time for
 * each call is within 5s.
 *
 * <p>12 mocked 2s remote service calls start sequentially. The 1 - 3 calls should start
 * immediately. The 4 - 6 calls should start one by one after the 1 - 3 calls finish. The 7 - 9
 * calls should start one by one after the 4 - 6 calls finish. The 10 - 12 calls should throw
 * "Bulkhead full of threads" exception after the 5s waiting time is over.
 */
@Slf4j
public class App {
  /**
   * Program entry point.
   *
   * @param args command line args.
   * @throws InterruptedException if an InterruptedException occurs.
   */
  public static void main(final String[] args) throws InterruptedException {
    final RemoteService remoteService = new RemoteService();
    final Bulkhead bulkhead = new SemaphoreBulkhead(3, 5000);
    final Runnable runnable = () -> remoteService.call();
    final Runnable runnableWithBulkhead = bulkhead.decorate(runnable);

    final Thread[] threads = new Thread[12];
    for (int i = 0; i < 12; i++) {
      final Thread t = new Thread(() -> {
        try {
          runnableWithBulkhead.run();
        } catch (final Exception e) {
          LOGGER.error("Exception: " + e.getMessage());
        }
      }, "Remote service call " + (i + 1));
      threads[i] = t;
      t.start();
      Thread.sleep(50);
    }

    for (Thread t : threads) {
      t.join();
    }
  }
}