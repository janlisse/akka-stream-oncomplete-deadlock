
import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.trademachines.scraper.{HttpClient, Scraper}
import org.jsoup.nodes.Document
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

import scala.concurrent.Future

class ScraperSpec extends FlatSpec with Matchers with MockitoSugar {


  it should "complete paginated (feedback) flow" in {

    val httpClient = mock[HttpClient]
    val scraper = new Scraper(httpClient)
    val testF = (doc: Document) => ("test", None)

    val url = new URL("http://test.de/auction")
    val html =
      """
        |<!DOCTYPE html>
        |<html lang="en">
      """.stripMargin

    when(httpClient.getDocument(url)).thenReturn(Future.successful((url, html)))

    implicit val system = ActorSystem("Sys")
    implicit val ec = system.dispatcher
    implicit val materializer = ActorFlowMaterializer()

    val flow = scraper.scrapePaginated(testF)
    Source(List(url))
      .via(flow)
      .runWith(TestSink.probe[String])
      .request(1)
      .expectNext("test")
      .expectComplete()
  }
}