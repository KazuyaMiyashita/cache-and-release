package rdb.ctx

trait IOContext[F[_], Ctx] {

  def init: Ctx
  def transaction[T](execution: Ctx => F[T]): F[T]
  def run(): Unit

}
