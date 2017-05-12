/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 Laird Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package weld.weld570;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import javax.inject.Inject;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@ApplicationScoped
public class TestScenario3 {

  private final CountDownLatch latch;
  
  @Inject
  private Event<CountEvent> eventBroadcaster;
  
  public TestScenario3() {
    super();
    this.latch = new CountDownLatch(1);
  }

  public void start(@Observes @Initialized(ApplicationScoped.class) final Object containerIsUp) throws InterruptedException {
    System.out.println("*** Telling counter to count.");
    final CountEvent event = new CountEvent();
    eventBroadcaster.fireAsync(event, NotificationOptions.of("weld.async.notification.mode", "PARALLEL"));
    System.out.println("*** Told counter to count.");
    System.out.println("*** Note that only one counting method is called, despite the PARALLEL notification mode.");
    System.out.println("*** Waiting for ten seconds.");
    this.latch.await(10L, TimeUnit.SECONDS);
  }

  public void count(@ObservesAsync final CountEvent countPlease) throws InterruptedException {
    int i = 0;
    while (true) {
      System.out.println(Thread.currentThread() + " " + this.getClass().getName() + ": " + i++ + "...");
      Thread.sleep(200L);
    }
  }

  public void moreCounting(@ObservesAsync final CountEvent countPlease, final DelegateCounter counter) throws InterruptedException {
    counter.count();
  }
  
  public void stop(@Observes @BeforeDestroyed(ApplicationScoped.class) final Object containerIsGoingDown) throws InterruptedException {
    System.out.println("*** Shutting down");
  }

  @Test
  public void startContainer() {
    final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
    assertNotNull(initializer);
    initializer.disableDiscovery();
    initializer.addBeanClasses(this.getClass());
    initializer.addBeanClasses(DelegateCounter.class);
    try (final SeContainer container = initializer.initialize()) {
      assertNotNull(container);
    }
  }

  @ApplicationScoped
  public static class DelegateCounter {

    public DelegateCounter() {
      super();
    }

    public void count() throws InterruptedException {
      int i = 0;
      while (true) {
        System.out.println(Thread.currentThread() + " " + this.getClass().getName() + ": " + i++ + "...");
        Thread.sleep(200L);
      }
    }

    public void start(@Observes @Initialized(ApplicationScoped.class) final Object containerIsUp) {
      System.out.println("*** " + Thread.currentThread() + " " + this.getClass().getName() + ": container is up");
    }
    
    public void stop(@Observes @BeforeDestroyed(ApplicationScoped.class) final Object containerIsGoingDown) {
      System.out.println("*** " + Thread.currentThread() + " " + this.getClass().getName() + ": container is going down");
    }
    
  }
  
}
