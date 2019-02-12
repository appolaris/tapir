package tapir

sealed trait DecodeResult[+T] {
  def getOrThrow(e: (DecodeResult[Nothing], Option[Throwable]) => Throwable): T
  def map[TT](f: T => TT): DecodeResult[TT]
  def flatMap[U](f: T => DecodeResult[U]): DecodeResult[U]
}
sealed trait DecodeFailure extends DecodeResult[Nothing] {
  def getOrThrow(e: (DecodeResult[Nothing], Option[Throwable]) => Throwable): Nothing = throw e(this, None)
  override def map[TT](f: Nothing => TT): DecodeResult[TT] = this
  override def flatMap[U](f: Nothing => DecodeResult[U]): DecodeResult[U] = this
}
object DecodeResult {
  case class Value[T](v: T) extends DecodeResult[T] {
    def getOrThrow(e: (DecodeResult[Nothing], Option[Throwable]) => Throwable): T = v
    override def map[TT](f: T => TT): DecodeResult[TT] = Value(f(v))
    override def flatMap[U](f: T => DecodeResult[U]): DecodeResult[U] = f(v)
  }
  case object Missing extends DecodeFailure // TODO: add field name?
  case class Multiple[R](vs: List[R]) extends DecodeFailure
  case class Error(original: String, error: Throwable) extends DecodeFailure
  case class Mismatch(expected: String, actual: String) extends DecodeFailure

  def sequence[T](results: List[DecodeResult[T]]): DecodeResult[List[T]] = {
    results.foldRight(Value(List.empty[T]): DecodeResult[List[T]]) {
      case (result, acc) =>
        (result, acc) match {
          case (Value(v), Value(vs))  => Value(v :: vs)
          case (Value(_), r)          => r
          case (df: DecodeFailure, _) => df
        }
    }
  }
}
