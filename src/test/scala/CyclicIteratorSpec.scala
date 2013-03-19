package com.pongr.jetray

import org.specs2.mutable._

class CyclicIteratorSpec extends Specification {
  "The CyclicIterator" should {
    "cycle through 1 element" in {
      val c = CyclicIterator("a")
      for (i <- 1 to 3) {
        c.hasNext must beTrue
        c.next must_== "a"
      }
    }

    "cycle through 2 elements" in {
      val c = CyclicIterator("a", "b")
      for (i <- 1 to 3) {
        c.hasNext must beTrue
        c.next must_== "a"
        c.hasNext must beTrue
        c.next must_== "b"
      }
    }

    "cycle through 3 elements" in {
      val c = CyclicIterator("a", "b", "c")
      for (i <- 1 to 3) {
        c.hasNext must beTrue
        c.next must_== "a"
        c.hasNext must beTrue
        c.next must_== "b"
        c.hasNext must beTrue
        c.next must_== "c"
      }
    }
  }
}
