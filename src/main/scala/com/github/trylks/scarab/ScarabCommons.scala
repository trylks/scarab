package com.github.trylks.scarab

import java.io.Closeable
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.annotation.tailrec

object ScarabCommons {
    def using[T <% Closeable, R](resource: T)(block: (T => R)): R = {
        try {
            block(resource)
        } finally {
            resource.close()
        }
    }

    def futurize[A, B](f: A => B): Future[A] => Future[B] = toFuture(fromFuture(f))

    def toFuture[A, B](f: A => B): A => Future[B] = x => future { f(x) }

    def fromFuture[A, B](f: A => B): Future[A] => B = x => f(Await.result(x, Duration.Inf))

    def nonull[A](a: A): Option[A] =
        if (a == null)
            None
        else
            Some(a)

}