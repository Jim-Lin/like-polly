name := """like-polly"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

classpathTypes += "maven-plugin"

libraryDependencies += "org.bytedeco" % "javacv" % "1.2"
libraryDependencies += "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2"
libraryDependencies += "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2" classifier "linux-x86"
libraryDependencies += "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2" classifier "linux-x86_64"
libraryDependencies += "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2" classifier "macosx-x86_64"
libraryDependencies += "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2" classifier "windows-x86"
libraryDependencies += "org.bytedeco.javacpp-presets" % "opencv" % "3.1.0-1.2" classifier "windows-x86_64"
