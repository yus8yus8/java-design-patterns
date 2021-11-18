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

import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;

/**
 * The Bulkhead pattern is a type of application design that is tolerant of failure. In a bulkhead
 * architecture, elements of an application are isolated into pools so that if one fails, the others
 * will continue to function.
 *
 * <p>In the below example, it uses a bulkhead to control the calls to the remote service. The
 * number of maximum concurrent calls is set to 5, and the waiting time is 5s.
 *
 * <p>20 remote service calls are called sequentially. The 1 - 5 calls should start immidiately.
 * The 6 - 10 calls should start one by one once 1 - 5 calls finishes. The 11 - 15 calls should
 * start one by one after 6 - 10 calls finishes. The 16 - 20 calls should fail after 5s waiting
 * time is over.
 */
@Slf4j
public class App {
  /**
   * Program entry point.
   *
   * @param args command line args.
   * @throws Exception if any error occurs.
   */
  public static void main(final String[] args) throws Exception {
    final RemoteService remoteService = new RemoteService();
    final SemaphoreBulkhead bulkhead = new SemaphoreBulkhead(5, 5000);
    final Runnable runnable = () -> remoteService.call();
    final Runnable runnableWithBulkhead = bulkhead.decorate(runnable);

    final Thread[] threads = new Thread[20];
    for (int i = 0; i < 20; i++) {
      final Thread t = new Thread(() -> {
        try {
          runnableWithBulkhead.run();
        } catch (final Exception e) {
          System.out.println(LocalTime.now() + " " + Thread.currentThread().getName()
                  + ": " + e.getMessage());
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