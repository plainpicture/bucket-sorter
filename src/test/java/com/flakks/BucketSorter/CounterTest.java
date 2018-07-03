package com.flakks.BucketSorter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CounterTest extends TestCase {
  public void testGetValue() {
    Counter counter = new Counter(1);

    assertEquals(1, counter.getValue());
  }

  public void testSetValue() {
    Counter counter = new Counter(1);
    counter.setValue(2);

    assertEquals(2, counter.getValue());
  }

  public void testIncrement() {
    Counter counter = new Counter(1);
    counter.increment();

    assertEquals(2, counter.getValue());
  }
}
