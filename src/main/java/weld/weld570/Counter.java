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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Initialized;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;

import javax.inject.Inject;

@ApplicationScoped
public class Counter {

  private final Event<Object> eventBroadcaster;

  @Inject
  public Counter(final Event<Object> eventBroadcaster) {
    super();
    this.eventBroadcaster = eventBroadcaster;
  }

  public void start(@Observes @Initialized(ApplicationScoped.class) final Object containerIsUp) {
    System.out.println("*** Telling counter to count.");
    eventBroadcaster.fireAsync(new Object()).thenRun(() -> System.out.println("*** All done counting; mathematics is a sham."));
    System.out.println("*** Told counter to count.");
  }

  public void stop(@Observes @BeforeDestroyed(ApplicationScoped.class) final Object containerIsGoingDown) {
    System.out.println("*** Shutting down");
  }

  public void count(@ObservesAsync final Object countPlease) {
    int i = 0;
    while (true) {
      System.out.println(i++ + "...");
    }
  }
  
}
