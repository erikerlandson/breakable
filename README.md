# breakable
A lightweight library for 'break' and 'continue' in Scala for-comprehensions


## Examples

The `breakable` library implements the `break` and `continue` operators on Scala sequences in `for` comprehensions.
```scala
scala> import com.manyangled.breakable._
import com.manyangled.breakable._

scala> val bkb1 = for {
     |   (x, xLab) <- (0 to 1000000).breakable
     | } yield {
     |   if (x % 2 == 1) continue(xLab)
     |   if (x > 10) break(xLab)
     |   x
     | }
bkb1: com.manyangled.breakable.Breakable[Int] = com.manyangled.breakable.Breakable@7e02ffa5

scala> bkb1.toList
res0: List[Int] = List(0, 2, 4, 6, 8, 10)

scala> bkb1.toVector
res1: Vector[Int] = Vector(0, 2, 4, 6, 8, 10)

scala> bkb1.toArray
res2: Array[Int] = Array(0, 2, 4, 6, 8, 10)

scala> bkb1.toIterator
res3: Iterator[Int] = non-empty iterator

scala> bkb1.toStream
res4: Stream[Int] = Stream(0, ?)
```
