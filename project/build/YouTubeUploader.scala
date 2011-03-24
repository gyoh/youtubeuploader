import sbt._

class YouTubeUploader(info: ProjectInfo) extends DefaultProject(info) {
  val scalatoolsRelease = "Scala Tools Snapshot" at
    "http://scala-tools.org/repo-releases/"
  val gdataRepo = "Mandubian Maven Google Repository" at
    "http://mandubian-mvn.googlecode.com/svn/trunk/mandubian-mvn/repository"

  override def libraryDependencies = Set(
    "com.google.gdata" % "gdata-youtube-2.0" % "1.41.5" % "compile->default",
    "javax.mail" % "mail" % "1.4" % "compile->default",
    "org.apache.httpcomponents" % "httpclient" % "4.1" % "compile->default",
    "ch.qos.logback" % "logback-classic" % "0.9.26",
    "junit" % "junit" % "4.5" % "test->default",
    "org.scala-tools.testing" %% "specs" % "1.6.6" % "test->default"
  ) ++ super.libraryDependencies
}