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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SemaphoreBulkhead}.
 */
class SemaphoreBulkheadTest {
  private RemoteService remoteService;
  private Runnable runnable;

  /**
   * This method sets up the remoteService and runnable for this test class.
   * It is executed before the execution of every test.
   */
  @BeforeEach
  void setUp() {
    remoteService = new RemoteService();
    runnable = () -> remoteService.call();
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1327
  /**
   * Test one thread should succeed without exception when the capacity of bulkhead is two.
   */
  @Test
  void shouldSucceedWhenBulkheadNotFull() throws InterruptedException {
    final SemaphoreBulkhead bulkhead = new SemaphoreBulkhead(2, 1000);
    final Runnable runnableWithBulkhead = bulkhead.decorate(runnable);

    final AtomicReference<Exception> exception1 = new AtomicReference<>();

    final Thread thread1 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception1));

    thread1.start();
    thread1.join();

    assertNull(exception1.get());
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1327
  /**
   * Test two threads should succeed without exceptions when the capacity of bulkhead is two.
   */
  @Test
  void shouldSucceedWhenBulkheadJustFull() throws InterruptedException {
    final SemaphoreBulkhead bulkhead = new SemaphoreBulkhead(2, 5000);
    final Runnable runnableWithBulkhead = bulkhead.decorate(runnable);

    final AtomicReference<Exception> exception1 = new AtomicReference<>();
    final AtomicReference<Exception> exception2 = new AtomicReference<>();

    final Thread thread1 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception1));
    final Thread thread2 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception2));

    thread1.start();
    thread2.start();

    thread1.join();
    thread2.join();

    assertNull(exception1.get());
    assertNull(exception2.get());
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1327
  /**
   * Bulkhead has a capacity of one and timeout value 0.5s.
   * Start two threads around the same time.
   * Each thread will run for 2s.
   *
   * <p>Test the first thread should succeed without exceptions.
   * Test the second thread should fail with "Bulkhead full of threads" exception because after
   * waiting 0.5s, the first thread is still not finished.
   */
  @Test
  void shouldThrowExceptionWhenBulkheadStillFullAfterWaitingTime() throws InterruptedException {
    final SemaphoreBulkhead bulkhead = new SemaphoreBulkhead(1, 500);
    final Runnable runnableWithBulkhead = bulkhead.decorate(runnable);

    final AtomicReference<Exception> exception1 = new AtomicReference<>();
    final AtomicReference<Exception> exception2 = new AtomicReference<>();

    final Thread thread1 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception1));
    final Thread thread2 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception2));

    thread1.start();
    Thread.sleep(50);
    thread2.start();

    thread1.join();
    thread2.join();

    assertNull(exception1.get());
    assertEquals(exception2.get().getMessage(), "Bulkhead full of threads");
  }

  // CS427 Issue link: https://github.com/iluwatar/java-design-patterns/issues/1327
  /**
   * Bulkhead has a capacity of one and timeout value 5s.
   * Start two threads around the same time.
   * Each thread will run for 2s.
   *
   * <p>Test the first thread should succeed without exception.
   * Test the second thread should succeed without exception because it waits 2s
   * for the first thread to finish and it is still within the timeout value 5s.
   */
  @Test
  void shouldSucceedWhenBulkheadAvailableAgain() throws InterruptedException {
    final SemaphoreBulkhead bulkhead = new SemaphoreBulkhead(1, 5000);
    final Runnable runnableWithBulkhead = bulkhead.decorate(runnable);

    final AtomicReference<Exception> exception1 = new AtomicReference<>();
    final AtomicReference<Exception> exception2 = new AtomicReference<>();

    final Thread thread1 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception1));
    final Thread thread2 = new Thread(
            constructRunnableWithException(runnableWithBulkhead, exception2));

    thread1.start();
    Thread.sleep(50);
    thread2.start();

    thread1.join();
    thread2.join();

    assertNull(exception1.get());
    assertNull(exception2.get());
  }

  private Runnable constructRunnableWithException(final Runnable runnable,
                                                  final AtomicReference<Exception> exception) {
    return () -> {
      try {
        runnable.run();
      } catch (final Exception e) {
        exception.set(e);
      }
    };
  }
}