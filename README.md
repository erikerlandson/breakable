## breakable
A lightweight library for 'break' and 'continue' in Scala for-comprehensions

### Use `breakable` in your project
[![Download](https://api.bintray.com/packages/manyangled/maven/breakable/images/download.svg?version=0.1.1) ](https://bintray.com/manyangled/maven/breakable/0.1.1/link)

##### SBT
```scala
resolvers += "manyangled" at "https://dl.bintray.com/manyangled/maven/"

libraryDependencies += "com.manyangled" %% "breakable" % "0.1.1"
```

##### API Doc
https://erikerlandson.github.io/breakable/latest/api/#package

### Examples
The `breakable` library implements the `break` and `continue` operators on Scala sequences in `for` comprehensions.  The result of a `for` comprehension over "breakable sequences" can be returned as a variety of standard Scala sequences:
```scala
scala> import com.manyangled.breakable._
import com.manyangled.breakable._

scala> val bkb1 = for {
     |   (x, xLab) <- (0 to 1000000).breakable  // create a "breakable sequence"
     | } yield {
     |   if (x % 2 == 1) continue(xLab)         // continue to next
     |   if (x > 10) break(xLab)                // break out of the loop
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

Breakable sequences can be created with a method or function.  Each breakable sequence exposes its own label (for example `xLab` and `yLab`, below).  The `break` and `continue` operators take a label, and send the looping "control" to the corresponding sequence in the `for` comprehension.  The `break` and `continue` operations may be used inside the `yield`, as in the previous example, or in `if` filters of a `for` comprehension, as in this example:
```scala
scala> import com.manyangled.breakable._
import com.manyangled.breakable._

scala> val bkb2 = for {
     |   (x, xLab) <- Stream.from(0).breakable   // create breakable sequence with a method
     |   (y, yLab) <- breakable(Stream.from(0))  // create with a function
     |   if (x % 2 == 1) continue(xLab)          // continue to next in outer "x" loop
     |   if (y % 2 == 0) continue(yLab)          // continue to next in inner "y" loop
     |   if (x > 10) break(xLab)                 // break the outer "x" loop
     |   if (y > x) break(yLab)                  // break the inner "y" loop
     | } yield (x, y)
bkb2: com.manyangled.breakable.Breakable[(Int, Int)] = com.manyangled.breakable.Breakable@34dc53d2

scala> bkb2.toVector
res0: Vector[(Int, Int)] = Vector((2,1), (4,1), (4,3), (6,1), (6,3), (6,5), (8,1), (8,3), (8,5), (8,7), (10,1), (10,3), (10,5), (10,7), (10,9))
```

The result of any `for` comprehension over breakable sequences is represented as a lazy computation, as shown by this example having logically infinite output.  This lazy output is stack-safe to evaluate, and is also memory-safe when traversed as an iterator:
```scala
scala> val bkb3 = for {
     |   (x, xLab) <- Stream.from(0).breakable
     | } yield {
     |   if (x % 2 == 1) continue (xLab)
     |   x
     | }
bkb3: com.manyangled.breakable.Breakable[Int] = com.manyangled.breakable.Breakable@65aad230

scala> bkb3.toStream.take(10).toVector
res0: Vector[Int] = Vector(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)

scala> bkb3.toIterator.take(100000000).length
res1: Int = 100000000
```
