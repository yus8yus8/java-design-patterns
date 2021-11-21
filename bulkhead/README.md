---
layout: pattern
title: Bulkhead
folder: bulkhead
permalink: /patterns/bulkhead/
categories: Behavioral
language: en
tags:
 - Cloud distributed
---

## Intent

A type of application design that is tolerant of failure. In a bulkhead architecture, elements of an application are isolated into pools so that if one fails, it will not exhaust resources of other applications. The others will continue to function.

## Explanation

Real world example

> A server has a default request handling thread pool size of 200 threads.
> One application service A calls Redis which has a problem and never finishes.
> Since other threads always finish, gradually all threads will be used by application service A and blocked by Redis.
> The server cannot take requests from other application service anymore.

In plain words

> Bulkhead limits the resources one component could use.

Wikipedia says

> A stability pattern used to protect distributed software applications.

**Programmatic Example**

The `Bulkhead` interface has a method to decorate runnable with the bulkhead constraint.
It limits the max number of concurrent calls.

```java
/**
 * A bulkhead can be used to decorate runnable and limit the number of parallel threads.
 */
public interface Bulkhead {

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
```

One implementation of `bulkhead` is using `Semaphore`.

```java
/**
 * A bulkhead implementation based on a semaphore.
 */
public class SemaphoreBulkhead implements Bulkhead {

  private final Semaphore semaphore;

  private final long timeout;

  /**
   * Creates a bulkhead with the max number of concurrent calls and a timeout value.
   *
   * @param maxConcurrentCalls the max number of concurrent calls the bulkhead allows.
   * @param timeout the timeout value a call should wait when the bulkhead is full.
   */
  public SemaphoreBulkhead(final int maxConcurrentCalls, final long timeout) {
    this.timeout = timeout;
    this.semaphore = new Semaphore(maxConcurrentCalls, true);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalThreadStateException when the bulkhead is full after a timeout value
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
```

A mocked `RemoteService` represents the remote call which could block.

```java
/**
 * A mocked remote service.
 */
@Slf4j
public class RemoteService {

  /**
   * A mocked remote call which takes 2 seconds to finish.
   */
  public void call() {
    try {
      LOGGER.info("starts");
      Thread.sleep(2000);
      LOGGER.info("finishes");
    } catch (final InterruptedException e) {
      LOGGER.error("Thread interrupted: ", e);
    }
  }
}
```

Finally the app simulates the behavior when 12 threads make calls to the remote service.

```java
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
```

The App log output:

```java
07:43:44.399 [Remote service call 1] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:44.442 [Remote service call 2] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:44.496 [Remote service call 3] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:46.412 [Remote service call 1] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:46.413 [Remote service call 4] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:46.446 [Remote service call 2] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:46.447 [Remote service call 5] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:46.497 [Remote service call 3] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:46.497 [Remote service call 6] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:48.414 [Remote service call 4] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:48.415 [Remote service call 7] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:48.448 [Remote service call 5] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:48.449 [Remote service call 8] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:48.499 [Remote service call 6] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:48.499 [Remote service call 9] INFO com.iluwatar.bulkhead.RemoteService - starts
07:43:49.875 [Remote service call 10] ERROR com.iluwatar.bulkhead.App - Exception: Bulkhead full of threads
07:43:49.917 [Remote service call 11] ERROR com.iluwatar.bulkhead.App - Exception: Bulkhead full of threads
07:43:49.973 [Remote service call 12] ERROR com.iluwatar.bulkhead.App - Exception: Bulkhead full of threads
07:43:50.420 [Remote service call 7] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:50.450 [Remote service call 8] INFO com.iluwatar.bulkhead.RemoteService - finishes
07:43:50.500 [Remote service call 9] INFO com.iluwatar.bulkhead.RemoteService - finishes
```

## Class diagram

![alt text](./etc/bulkhead.urm.png "Bulkhead class diagram")

## Applicability

Use the Bulkhead in any of the following situations:

* When you want to isolate resources used by a component.

## Credits

* [Resilience4j | Bulkhead basics & runtime behavior | Simple example for beginners](https://itsallbinary.com/resilience4j-bulkhead-basics-runtime-behavior-simple-example-for-beginners/)
* [resilience4j](https://github.com/resilience4j/resilience4j/tree/master/resilience4j-bulkhead/src/main/java/io/github/resilience4j)
* [Implementing Bulkhead with Resilience4j](https://reflectoring.io/bulkhead-with-resilience4j/)