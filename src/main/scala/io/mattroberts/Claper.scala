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

  implicit def genericParser[A, R <: HList, D <: HList](
    implicit
    defaults: Default.AsOptions.Aux[A, D],
    generic: LabelledGeneric.Aux[A, R],
    parser: Lazy[UnderlyingClaper[R, D]]
  ): Claper[A] = {
    create { args => parser.value.parse(args, defaults()).right.map(generic.from) }
  }

  private def create[A](thunk: Seq[String] => ClaperError Or A): Claper[A] = {
    new Claper[A] {
      def parse(args: Seq[String]): ClaperError Or A = thunk(args)
    }
  }
}

trait UnderlyingClaper[A, B] {
  import Claper.Or

  def parse(args: Seq[String], defaults: B): ClaperError Or A
}

object UnderlyingClaper {
  import Claper.Or

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
        h <- hv.right
        t <- tv.right
      } yield h :: t
    }
  }

  implicit def optionParser[A, K <: Symbol](
    implicit
    witness: Witness.Aux[K],
    aParser: UnderlyingClaper[FieldType[K, A], Option[A]]
  ): UnderlyingClaper[FieldType[K, Option[A]], Option[Option[A]]] = {
    val name = witness.value.name
    create { (args, default) =>
      val argDefined = args.find(a => a == s"--$name").isDefined
      if (argDefined) {
        aParser.parse(args, default.flatten).right.map(v => field[K](Option(v)))
      } else {
        Right(field[K](None))
      }
    }
  }

  implicit def stringParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, String], Option[String]] = {
    createWithWitness(_.toString)
  }

  implicit def byteParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Byte], Option[Byte]] = {
    createWithWitness(_.toByte)
  }

  implicit def shortParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Short], Option[Short]] = {
    createWithWitness(_.toShort)
  }

  implicit def intParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Int], Option[Int]] = {
    createWithWitness(_.toInt)
  }

  implicit def longParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Long], Option[Long]] = {
    createWithWitness(_.toLong)
  }

  implicit def floatParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Float], Option[Float]] = {
    createWithWitness(_.toFloat)
  }

  implicit def doubleParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Double], Option[Double]] = {
    createWithWitness(_.toDouble)
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

  implicit def charParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, Char], Option[Char]] = {
    createWithWitness(_(0))
  }

  private def create[A, B](
    thunk: (Seq[String], B) => ClaperError Or A
  ): UnderlyingClaper[A, B] = {
    new UnderlyingClaper[A, B] {
      def parse(args: Seq[String], defaults: B): ClaperError Or A = {
        thunk(args, defaults)
      }
    }
  }

  private def createWithWitness[A, K <: Symbol](
    thunk: String => A
  )(
    implicit
    witness: Witness.Aux[K]
  ): UnderlyingClaper[FieldType[K, A], Option[A]] = {
    new UnderlyingClaper[FieldType[K, A], Option[A]] {
      def parse(args: Seq[String], defaultArg: Option[A]): ClaperError Or FieldType[K, A] = {
        val name = witness.value.name
        val providedArg = getArgFor(args, name).map(thunk)
        providedArg.map(Right(_)).getOrElse(
          defaultArg.map(Right(_)).getOrElse(
            Left(ClaperError(s"Missing argument $name"))
          )
        ).right.map(a => field[K](a))
      }
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
