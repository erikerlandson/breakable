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

package com.manyangled

package object breakable {
  import Breakable._

  def break(lab: Label): Unit = { throw Break(lab) }
  def continue(lab: Label): Unit = { throw Continue(lab) }

  implicit class ImplicitForBreakingFilter(b: => Boolean) {
    def break(lab: Label): Boolean = {
      if (b) throw Break(lab)
      true
    }
    def continue(lab: Label): Boolean = {
      if (b) throw Continue(lab)
      true
    }
  }

  implicit class ImplicitForSequenceResults[A](bkb: Breakable[A]) {
    def toStream: Stream[A] = bkb.rawStream.map(_.get).asInstanceOf[Stream[A]]
    def toIterator: Iterator[A] = toStream.iterator
    def toVector: Vector[A] = toStream.toVector
  }

  def breakable[A](s: => Seq[A]): Breakable[(A, Label)] = Breakable(s)

  implicit class ImplicitForBreakableMethod[A](s: => Seq[A]) {
    def breakable: Breakable[(A, Label)] = Breakable(s)
  }
}
