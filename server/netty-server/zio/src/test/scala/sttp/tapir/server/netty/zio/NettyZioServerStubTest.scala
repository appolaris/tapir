package sttp.tapir.server.netty.zio

import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.interceptor.CustomiseInterceptors
import sttp.tapir.server.tests.{CreateServerStubTest, ServerStubTest}
import sttp.tapir.ztapir.RIOMonadError
import zio.{Runtime, Task, Unsafe}

import java.net.InetSocketAddress
import scala.concurrent.Future

class NettyZioCreateServerStubTest extends CreateServerStubTest[Task, NettyZioServerOptions[Any, InetSocketAddress]] {

  override def customiseInterceptors: CustomiseInterceptors[Task, NettyZioServerOptions[Any, InetSocketAddress]] =
    NettyZioServerOptions.customiseInterceptors
  override def stub[R]: SttpBackendStub[Task, R] = SttpBackendStub(new RIOMonadError[Any]())

  override def asFuture[A]: Task[A] => Future[A] = task => Unsafe.unsafe(implicit u => Runtime.default.unsafe.runToFuture(task))

}

class NettyZioServerStubTest extends ServerStubTest(new NettyZioCreateServerStubTest)
