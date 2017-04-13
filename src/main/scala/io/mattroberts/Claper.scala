package io.mattroberts

import shapeless._
import shapeless.labelled._

final case class ClaperError(message: String) {
  def msg: String = message
}

trait Claper[A] {
  import Claper.Or
  def parse(args: Seq[String]): ClaperError Or A
}

object Claper {
  type Or[A, B] = Either[A, B]

  def apply[A](
    implicit
    st: Lazy[Claper[A]]
  ): Claper[A] = st.value

  def create[A](thunk: Seq[String] => ClaperError Or A): Claper[A] = {
    new Claper[A] {
      def parse(args: Seq[String]): ClaperError Or A = thunk(args)
    }
  }

  implicit def genericParser[A, R <: HList, D <: HList](
    implicit
    defaults: Default.AsOptions.Aux[A, D],
    generic: LabelledGeneric.Aux[A, R],
    parser: Lazy[UnderlyingClaper[R, D]]
  ): Claper[A] = {
    create { args => parser.value.parse(args, defaults()).map(generic.from) }
  }
}

trait UnderlyingClaper[A, B] {
  import Claper.Or

  def parse(args: Seq[String], defaults: B): ClaperError Or A
}

object UnderlyingClaper {
  import Claper.Or

  def apply[A, B](
    implicit
    st: Lazy[UnderlyingClaper[A, B]]
  ): UnderlyingClaper[A, B] = st.value

  def create[A, B](
    thunk: (Seq[String], B) => ClaperError Or A
  ): UnderlyingClaper[A, B] = {
    new UnderlyingClaper[A, B] {
      def parse(args: Seq[String], defaults: B): ClaperError Or A = {
        thunk(args, defaults)
      }
    }
  }

  implicit val hnilParser: UnderlyingClaper[HNil, HNil] = {
    create { (_, _) => Right(HNil) }
  }

  implicit def hlistParser[K <: Symbol, H, T <: HList, TD <: HList](
    implicit
    witness: Witness.Aux[K],
    hParser: Lazy[UnderlyingClaper[FieldType[K, H], Option[H]]],
    tParser: UnderlyingClaper[T, TD]
  ): UnderlyingClaper[FieldType[K, H] :: T, Option[H] :: TD] = {
    create { (args, defaults) =>
      val hv = hParser.value.parse(args, defaults.head)
      val tv = tParser.parse(args, defaults.tail)
      for {
        h <- hv
        t <- tv
      } yield h :: t
    }
  }

  implicit def stringParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, String], Option[String]] = {
    val name = witness.value.name
    create { (args, defaultArg) =>
      val providedArg = getArgFor(args, name)
      providedArg.map(Right(_)).getOrElse(
        defaultArg.map(Right(_)).getOrElse(
          Left(ClaperError(s"Missing argument $name"))
        )
      ).map(field[K](_))
    }
  }

  implicit def intParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Int], Option[Int]] = {
    val name = witness.value.name
    create { (args, defaultArg) =>
      val providedArg = getArgFor(args, name).map(_.toInt)
      providedArg.map(Right(_)).getOrElse(
        defaultArg.map(Right(_)).getOrElse(
          Left(ClaperError(s"Missing argument $name"))
        )
      ).map(field[K](_))
    }
  }

  implicit def booleanParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Boolean], Option[Boolean]] = {
    val name = witness.value.name
    create { (args, default) =>
      val arg = args.find(a => a == s"--$name").isDefined
      Right(field[K](arg))
    }
  }

  private def getArgFor(args: Seq[String], name: String): Option[String] = {
    val indexOfName = args.indexOf(s"--$name")
    val indexAfterName = indexOfName + 1
    if (indexOfName > -1 && args.isDefinedAt(indexAfterName)) {
      Some(args(indexAfterName))
    } else {
      None
    }
  }
}
