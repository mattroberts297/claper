package io.mattroberts

trait LabelledParser[A] {
  def parse(args: List[String]): A
}

object LabelledParser {
  import shapeless._
  import shapeless.labelled._

  def create[A](thunk: List[String] => A): LabelledParser[A] = {
    new LabelledParser[A] {
      def parse(args: List[String]): A = thunk(args)
    }
  }

  implicit def stringParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): LabelledParser[FieldType[K, String]] = {
    val name = witness.value.name
    create { args =>
      val arg = args.dropWhile(a => a != s"--$name").tail.head
      field[K](arg)
    }
  }

  implicit def intParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): LabelledParser[FieldType[K, Int]] = {
    val name = witness.value.name
    create { args =>
      val arg = args.dropWhile(a => a != s"--$name").tail.head.toInt
      field[K](arg)
    }
  }

  implicit def booleanParser[K <: Symbol](
    implicit
    witness: Witness.Aux[K]
  ): LabelledParser[FieldType[K, Boolean]] = {
    val name = witness.value.name
    create { args =>
      val arg = args.find(a => a == s"--$name").isDefined
      field[K](arg)
    }
  }

  implicit val hnilParser: LabelledParser[HNil] = {
    create(args => HNil)
  }

  implicit def hlistLabelledParser[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    hParser: Lazy[LabelledParser[FieldType[K, H]]],
    tParser: LabelledParser[T]
  ): LabelledParser[FieldType[K, H] :: T] = {
    create { args =>
      val hv = hParser.value.parse(args)
      val tv = tParser.parse(args)
      hv :: tv
    }
  }

  implicit def genericLabelledParser[A, R <: HList](
    implicit
    generic: LabelledGeneric.Aux[A, R],
    parser: Lazy[LabelledParser[R]]
  ): LabelledParser[A] = {
    create(args => generic.from(parser.value.parse(args)))
  }

  def apply[A](
    implicit
    st: Lazy[LabelledParser[A]]
  ): LabelledParser[A] = st.value
}
