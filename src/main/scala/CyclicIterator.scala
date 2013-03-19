package com.pongr.jetray

/** Iterator that continually cycles through the specified elements. */
case class CyclicIterator[A](as: A*) extends Iterator[A] {
  var count = 0
  override def next(): A = {
    val a = as(count % as.size)
    count += 1
    a
  }
  override def hasNext(): Boolean = true
}
