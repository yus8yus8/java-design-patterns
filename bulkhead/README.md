---
layout: pattern
title: Bulkhead
folder: bulkhead
permalink: /patterns/bulkhead/
categories: Behavioral
language: en
tags:
 - Cloud distributed
 - Availability
---

## Intent

A type of application design that is tolerant of failure. In a bulkhead architecture, elements of an application are isolated into pools so that if one fails, the others will continue to function.

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
It limits the number of concurrent call. If one runnable blocks, it will not exhaust all the threads in the server.

```java
public interface Bulkhead {

  /**
   * Decorates a runnable such that the runnable have to wait or timeout
   * when the number of conccurent threads has reached the bulkhead limit.
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

  private final int maxConcurrentCalls;

  /**
   * Creates a bulkhead with the max number of concurrent calls and timeout value.
   */
  public SemaphoreBulkhead(final int maxConcurrentCalls, final long timeout) {
    this.maxConcurrentCalls = maxConcurrentCalls;
    this.timeout = timeout;
    this.semaphore = new Semaphore(maxConcurrentCalls, true);
  }

  /**
   * {@inheritDoc}
   * @throws IllegalThreadStateException when the bulkhead is full
   * @throws CancellationException if the thread is interrupted during permission wait
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
      if (acquired == false) {
        throw new IllegalThreadStateException("Bulkhead full of threads");
      }
    } catch (final InterruptedException e) {
      throw new CancellationException("Bulkhead acquire permission cancelled", e);
    }
  }

  private void releasePermission() {
    semaphore.release();
  }
}
  ...
}
```

A mocked `RemoteService` represents the remote call which could block.

```java
/**
 * A mocked remote service.
 */
public class RemoteService {

  /**
   * A mocked remote call which takes 2 seconds to finish.
   */
  public void call() {
    try {
      System.out.println(LocalTime.now() + " " + Thread.currentThread().getName() + " starts");
      Thread.sleep(2000);
      System.out.println(LocalTime.now() + " " + Thread.currentThread().getName() + " finishes");
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
```

Finally the app simulates the behavior when 20 threads make calls to the remote service.

```java
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
```

The program output:

```java
22:57:00.976706 Remote service call 1 starts
22:57:01.026964 Remote service call 2 starts
22:57:01.077337 Remote service call 3 starts
22:57:01.127627 Remote service call 4 starts
22:57:01.178039 Remote service call 5 starts
22:57:02.977109 Remote service call 1 finishes
22:57:02.977520 Remote service call 6 starts
22:57:03.027525 Remote service call 2 finishes
22:57:03.027890 Remote service call 7 starts
22:57:03.077582 Remote service call 3 finishes
22:57:03.077869 Remote service call 8 starts
22:57:03.128411 Remote service call 4 finishes
22:57:03.128640 Remote service call 9 starts
22:57:03.178288 Remote service call 5 finishes
22:57:03.178524 Remote service call 10 starts
22:57:04.977725 Remote service call 6 finishes
22:57:04.977945 Remote service call 11 starts
22:57:05.028078 Remote service call 7 finishes
22:57:05.028388 Remote service call 12 starts
22:57:05.078090 Remote service call 8 finishes
22:57:05.078299 Remote service call 13 starts
22:57:05.128837 Remote service call 9 finishes
22:57:05.129063 Remote service call 14 starts
22:57:05.178737 Remote service call 10 finishes
22:57:05.178917 Remote service call 15 starts
22:57:06.731966 Remote service call 16: Bulkhead full of threads
22:57:06.782254 Remote service call 17: Bulkhead full of threads
22:57:06.832720 Remote service call 18: Bulkhead full of threads
22:57:06.883020 Remote service call 19: Bulkhead full of threads
22:57:06.933423 Remote service call 20: Bulkhead full of threads
22:57:06.978170 Remote service call 11 finishes
22:57:07.028587 Remote service call 12 finishes
22:57:07.078498 Remote service call 13 finishes
22:57:07.129283 Remote service call 14 finishes
22:57:07.179111 Remote service call 15 finishes
```

## Class diagram

TODO

## Applicability

Use the Bulkhead in any of the following situations:

* When you want to isolate resources used by a component.

## Credits

* [Resilience4j | Bulkhead basics & runtime behavior | Simple example for beginners](https://itsallbinary.com/resilience4j-bulkhead-basics-runtime-behavior-simple-example-for-beginners/)
* [resilience4j](https://github.com/resilience4j/resilience4j/tree/master/resilience4j-bulkhead/src/main/java/io/github/resilience4j)
* [Implementing Bulkhead with Resilience4j](https://reflectoring.io/bulkhead-with-resilience4j/)