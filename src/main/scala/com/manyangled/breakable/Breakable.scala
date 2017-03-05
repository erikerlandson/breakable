/*
Copyright 2017 Erik Erlandson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.manyangled.breakable

import scala.annotation.tailrec
import scala.util.{ Try, Success, Failure }

import Breakable._

class Breakable[A] private [breakable] (
    stream: () => Stream[Try[Any]],
    label: Label,
    p: A => Boolean) {

  private [breakable] def rawStream = stream()

  def map[B](f: A => B): Breakable[B] = {
    val g = (e: Any) => f((e, label).asInstanceOf[A])
    val q = (e: Any) => p((e, label).asInstanceOf[A])
    new Breakable[B](() => doMap(g, q, stream()), noLabel, pTrue[B])
  }

  private def doMap(g: Any => Any, q: Any => Boolean, s: Stream[Try[Any]]): Stream[Try[Any]] = {
    if (s.isEmpty) Stream.empty[Try[Any]] else {
      (s.head.map(q) match {
        case Success(true) => s.head.map(g)
        case Success(false) => Failure[Any](Continue(label))
        case f => f
      }) match {
        case h @ Success(_) => h #:: doMap(g, q, s.tail)
        case Failure(Continue(`label`)) => doMap(g, q, s.tail)
        case Failure(Break(`label`)) => Stream.empty[Try[Any]]
        case f @ Failure(_: Control) => Stream(f)
        case Failure(t: Throwable) => throw(t)
      }
    }
  }

  def flatMap[B](f: A => Breakable[B]): Breakable[B] = {
    val g = (e: Any) => f((e, label).asInstanceOf[A])
    val q = (e: Any) => p((e, label).asInstanceOf[A])
    new Breakable[B](() => doFlatMap(g, q, stream()), noLabel, pTrue[B])
  }

  private def doFlatMap[B](g: Any => Breakable[B], q: Any => Boolean, s: Stream[Try[Any]]):
      Stream[Try[Any]] = {
    if (s.isEmpty) Stream.empty[Try[Any]] else {
      (s.head.map(q) match {
        case Success(true) => s.head.map(g)
        case Success(false) => Failure[Breakable[B]](Continue(label))
        case f => f.asInstanceOf[Failure[Breakable[B]]]
      }) match {
        case Success(b) => doFlatMapContinue(b.rawStream, () => doFlatMap(g, q, s.tail))
        case Failure(Continue(`label`)) => doFlatMap(g, q, s.tail)
        case Failure(Break(`label`)) => Stream.empty[Try[Any]]
        case f @ Failure(_: Control) => Stream(f)
        case Failure(t: Throwable) => throw(t)
      }
    }
  }

  private def doFlatMapContinue(s: Stream[Try[Any]], cont: () => Stream[Try[Any]]):
      Stream[Try[Any]] = {
    if (s.isEmpty) cont() else s.head match {
      case h @ Success(_) => h #:: doFlatMapContinue(s.tail, cont)
      case Failure(Continue(`label`)) => cont()
      case Failure(Break(`label`)) => Stream.empty[Try[Any]]
      case f @ Failure(_: Control) => Stream(f)
      case Failure(t: Throwable) => throw(t)
    }
  }

  def foreach[U](f: A => U): Unit = {
    val g = (e: Any) => f((e, label).asInstanceOf[A])
    val q = (e: Any) => p((e, label).asInstanceOf[A])
    doForeach(g, q, stream())
  }

  @tailrec private def doForeach[U](g: Any => U, q: Any => Boolean, s: Stream[Try[Any]]): Unit = {
    if (!s.isEmpty) {
      (s.head.map(q) match {
        case Success(true) => s.head.map(g)
        case Success(false) => Failure[Any](Continue(label))
        case f => f
      }) match {
        case Success(_) => doForeach(g, q, s.tail)
        case Failure(Continue(`label`)) => doForeach(g, q, s.tail)
        case Failure(Break(`label`)) => ()
        case Failure(ctrl) => throw ctrl
      }
    }
  }

  def withFilter(q: A => Boolean): Breakable[A] =
    new Breakable[A](stream, label, a => p(a) && q(a))
}

object Breakable {
  trait Label

  sealed trait Control extends Exception
  case class Break(label: Label) extends Control
  case class Continue(label: Label) extends Control

  object noLabel extends Label

  def pTrue[A]: A => Boolean = (_: A) => true

  def apply[A](t: => Seq[A]) =
    new Breakable[(A, Label)](() => t.toStream.map(Success(_)), new Label {}, pTrue[(A, Label)])
}
