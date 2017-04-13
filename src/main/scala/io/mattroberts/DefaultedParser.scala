package io.mattroberts

import shapeless._
import shapeless.labelled._

trait DefaultedParser[A] {
  def parse(args: List[String]): A
}

object DefaultedParser {
  def apply[A](
    implicit
    st: Lazy[DefaultedParser[A]]
  ): DefaultedParser[A] = st.value

  def create[A](thunk: List[String] => A): DefaultedParser[A] = {
    new DefaultedParser[A] {
      def parse(args: List[String]): A = thunk(args)
    }
  }

  implicit def genericParser[A, R <: HList, D <: HList](
    implicit
    defaults: Default.AsOptions.Aux[A, D],
    generic: LabelledGeneric.Aux[A, R],
    parser: Lazy[UnderlyingParser[R, D]]
  ): DefaultedParser[A] = {
    create { args => generic.from(parser.value.parse(args, defaults())) }
  }
}

trait UnderlyingParser[A, B] {
  def parse(args: List[String], defaults: B): A
}

object UnderlyingParser {
  def apply[A, B](
    implicit
    st: Lazy[UnderlyingParser[A, B]]
  ): UnderlyingParser[A, B] = st.value

  def create[A, B](thunk: (List[String], B) => A): UnderlyingParser[A, B] = {
    new UnderlyingParser[A, B] {
      def parse(args: List[String], defaults: B): A = thunk(args, defaults)
    }
  }

  implicit val hnilParser: UnderlyingParser[HNil, HNil] = {
    create { (_, _) => HNil }
  }

  implicit def hlistParser[K <: Symbol, H, T <: HList, TD <: HList](
    implicit
    witness: Witness.Aux[K],
    hParser: Lazy[UnderlyingParser[FieldType[K, H], Option[H]]],
    tParser: UnderlyingParser[T, TD]
  ): UnderlyingParser[FieldType[K, H] :: T, Option[H] :: TD] = {
    create { (args, defaults) =>
      val hv = hParser.value.parse(args, defaults.head)
      val tv = tParser.parse(args, defaults.tail)
      hv :: tv
    }
  }

  implicit def stringParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingParser[FieldType[K, String], Option[String]] = {
    val name = witness.value.name
    create { (args, defaultArg) =>
      val providedArg = getArgFor(args, name)
      val arg = providedArg.getOrElse(
        defaultArg.getOrElse(
          // TODO: Use Either instead of throwing an exception.
          throw new IllegalArgumentException(s"Missing argument $name")
        )
      )
      field[K](arg)
    }
  }

  implicit def intParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingParser[FieldType[K, Int], Option[Int]] = {
    val name = witness.value.name
    create { (args, defaultArg) =>
      val providedArg = getArgFor(args, name).map(_.toInt)
      val arg = providedArg.getOrElse(
        defaultArg.getOrElse(
          // TODO: Use Either instead of throwing an exception.
          throw new IllegalArgumentException(s"Missing argument $name")
        )
      )
      field[K](arg)
    }
  }

  implicit def booleanParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingParser[FieldType[K, Boolean], Option[Boolean]] = {
    val name = witness.value.name
    create { (args, default) =>
      val arg = args.find(a => a == s"--$name").isDefined
      field[K](arg)
    }
  }

  private def getArgFor(args: List[String], name: String): Option[String] = {
    val indexOfName = args.indexOf(s"--$name")
    val indexAfterName = indexOfName + 1
    if (indexOfName > -1 && args.isDefinedAt(indexAfterName)) {
      Some(args(indexAfterName))
    } else {
      None
    }
  }
}
