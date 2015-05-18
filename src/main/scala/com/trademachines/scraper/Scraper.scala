package com.trademachines.scraper

import java.net.URL

import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.concurrent.Future


trait HttpClient {
  def getDocument(url: URL): Future[(URL, String)]
}

class Scraper(httpClient: HttpClient) {

  val toDocument = (content: (URL, String)) => {
    val doc = Jsoup.parse(content._2)
    doc.setBaseUri(content._1.toString)
    doc
  }

  val scrape: Flow[URL, Document, Unit] = Flow[URL].mapAsyncUnordered(10)(httpClient.getDocument)
    .buffer(10, OverflowStrategy.backpressure)
    .map(toDocument)

  def scrapePaginated[T](f: (Document) => (T, Option[URL])) = Flow() { implicit b =>
    import akka.stream.scaladsl.FlowGraph.Implicits._

    val merge = b.add(MergePreferred[URL](1))
    val unzip = b.add(Unzip[T, Option[URL]]())

    merge ~> scrape.map(f) ~> unzip.in
    unzip.out1.filter(_.isDefined).map(_.get) ~> Flow[URL].buffer(100, OverflowStrategy.fail) ~> merge.preferred

    (merge.in(0), unzip.out0)
  }
}
