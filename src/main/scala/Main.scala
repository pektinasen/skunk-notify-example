import cats.implicits._
import cats.effect._
import fs2._
import natchez.Trace.Implicits.noop
import skunk._
import skunk.codec.all._
import skunk.data._
import skunk.implicits._

object Main extends IOApp {

  println("─" * 100)

  val session: Resource[IO, Session[IO]] =
    Session.single( // (2)
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "postgres",
      password = Some("password")
    )

  def run(args: List[String]): IO[ExitCode] = {
    val insert : Command[String] =
      sql"""INSERT INTO texts (body) VALUES ($varchar)""".command
    val data = List("Hello", "World", "!")
    session.use { s =>
      val ch: Channel[IO, String, String] = s.channel(id"texts")
      val nbs: Stream[IO, Notification[String]] = ch.listen(420)
      for {
        fiber <- nbs.through(
          _.evalTap(a => IO(println(a.toString)))
        ).compile.drain.start
        _ <- data.traverse_(d => s.prepare(insert).use(_.execute(d)))
        _ <- fiber.join
      } yield ExitCode.Success
    }
  }

  println("─" * 100)
}