import io.Source
import tools.nsc.io.{Path, File}
import java.text.SimpleDateFormat
import java.util.Date
import java.net.URL

import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.BufferedHttpEntity
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

import com.google.gdata.client.youtube._
import com.google.gdata.data.media._
import com.google.gdata.data.media.mediarss._
import com.google.gdata.data.youtube._
import com.google.gdata.util._

object YouTubeUploader {

  val clientID = "YouTubeUploader"
  val developerKey = ""
  val uploadUrl = "http://uploads.gdata.youtube.com/feeds/api/users/default/uploads"
  val writeUrl = "http://www.tumblr.com/api/write"

  var count = 0  // number of videos uploaded

  def main(args: Array[String]) {
    require(args.length == 5)
    val youTubeName = args(0)     // YouTube name
    val youTubePassword = args(1) // YouTube password
    val tumblrName = args(2)      // tumblr name
    val tumblrEmail = args(3)     // tumblr email
    val tumblrPassword = args(4)  // tumblr password

    upload(Path("/home/gyo/Videos/family"),
      upload(youTubeName, youTubePassword, _),
      write(tumblrName, tumblrEmail, tumblrPassword, _, _))
  }


  // get videos recursively and upload them to youtube and post them to tumblr
  def upload(path: Path, uploadPF: File => VideoEntry, writePF: (File, String) => Unit): Unit = path.isFile match {
    case true =>
      val entry = uploadPF(path.toFile)
      writePF(path.toFile, entry.getHtmlLink.getHref)
    case false => path.walkFilter(
      p => p.isDirectory || p.extension.equalsIgnoreCase("mp4") || p.extension.equalsIgnoreCase("m4v"))
      .foreach(upload(_, uploadPF, writePF))
  }

  // upload video to YouTube
  def upload(name: String, password: String, file: File) = {
    val caption = getCaption(file)

    val service = new YouTubeService(clientID, developerKey)
    service.setUserCredentials(name, password)

    val newEntry = new VideoEntry

    val mg = newEntry.getOrCreateMediaGroup

    mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME, "People"))
    mg.setPrivate(false)
    mg.setTitle(new MediaTitle)
    mg.getTitle.setPlainTextContent(caption)
    mg.setDescription(new MediaDescription)
    mg.getDescription.setPlainTextContent(caption)
    val ms = new MediaFileSource(new java.io.File(file.path), "video/mp4")
    newEntry.setMediaSource(ms)

    val createdEntry = service.insert(new URL(uploadUrl), newEntry)

    // update to unlisted
    val xmlBlob = new XmlBlob()
    xmlBlob.setBlob("<yt:accessControl action='list' permission='denied'/>")
    createdEntry.setXmlBlob(xmlBlob)
    createdEntry.update

    createdEntry
  }

  // write to tumblr
  def write(name: String, email: String, password: String, file: File, link: String) {
    val caption = getCaption(file)
    val date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(getDate(file))
    println(file)
    println(caption)
    println(date)

    val httpClient = new DefaultHttpClient
    val httpPost = new HttpPost(writeUrl)

    // http://www.tumblr.com/docs/en/api#api_write
    val params = Map(
      "email" -> email,
      "password" -> password,
      "type" -> "video",
      "embed" -> link,
      "generator" -> "Shun Album",
      "date" -> date,
      "caption" -> caption,
      "group" -> "%s.tumblr.com".format(name),
      "send-to-twitter" -> "no"
    )

    // convert scala Iterable to java List
    val httpParams = new java.util.ArrayList[NameValuePair]
    params.map(pair => httpParams.add(new BasicNameValuePair(pair._1, pair._2)))

    httpPost.setEntity(new UrlEncodedFormEntity(httpParams, "UTF-8"))

    Thread.sleep(1000) // avoid exceeding tumblr rate limit

    count += 1
    println("Uploading photo No.%d".format(count))

    val httpResponse = httpClient.execute(httpPost)
    val statusLine = httpResponse.getStatusLine
    val statusCode = statusLine.getStatusCode
    val result = statusCode match {
      case 201 =>
        val c = new BufferedHttpEntity(httpResponse.getEntity).getContent
        "Success! The new post ID is %s".format(
          Source.fromInputStream(c).mkString)
      case 403 => "Bad email or password"
      case _ =>
        val c = new BufferedHttpEntity(httpResponse.getEntity).getContent
        "Error: %s".format(Source.fromInputStream(c).mkString)
    }
    println(result)
  }

  def getCaption(file: File) = {
    val caption = file.stripExtension
    caption.replace("_", " ")
  }

  def getDate(file: File) = {
      // set date extracted from the name of the file
      val datePattern = """(\d{8})""".r
      val firstWord = file.name.split("_")(0)
      firstWord match {
        case datePattern(firstWord) =>
          val sdf = new SimpleDateFormat("yyyyMMdd")
          sdf.parse("%s".format(firstWord))
        case _ => new Date
      }
  }
}