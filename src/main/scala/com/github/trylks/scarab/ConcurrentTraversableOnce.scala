package com.github.trylks.scarab

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class ConcurrentTraversableOnce[A, T <: TraversableOnce[A]](val innerTraversable: T) {
  def cmap[R](function: A => R) = {
    innerTraversable.map(x => Future { function(x) }).toIterator.map(Await.result(_, Duration.Inf))
  }
}
