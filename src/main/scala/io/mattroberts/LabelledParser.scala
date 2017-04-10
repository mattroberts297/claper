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

  implicit val stringParser: LabelledParser[String] = {
    create(args => args.head)
  }

  implicit val intParser: LabelledParser[Int] = {
    create(args => args.head.toInt)
  }

  implicit val boolParser: LabelledParser[Boolean] = {
    create(args => args.head.toBoolean)
  }

  implicit val hnilParser: LabelledParser[HNil] = {
    create(args => HNil)
  }

  implicit def hlistLabelledParser[K <: Symbol, H, T <: HList](
    implicit
    witness: Witness.Aux[K],
    hParser: Lazy[LabelledParser[H]],
    tParser: LabelledParser[T]
  ): LabelledParser[FieldType[K, H] :: T] = {
    create { args =>
      val name = witness.value.name
      val hs = args.dropWhile(a => a != s"--$name")
      val hv = hParser.value.parse(hs.tail)
      val tv = tParser.parse(args)
      field[K](hv) :: tv
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
