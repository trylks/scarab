lazy val personalSettings = Seq(
  organization := "org.github.trylks",
  version := "0.0.1",
  scalaVersion := "2.11.7" 
)

lazy val mainapp = Some("com.github.trylks.scarab.App")
//lazy val maintest = Some("com.github.trylks.scarab.TestSuite")

lazy val preferredSettings = Seq(
    // pollInterval := 1000,
    javacOptions ++= Seq("-source", "jvm-7", "-target", "jvm-7"),
    scalacOptions += "-deprecation",
    scalaSource in Compile := baseDirectory.value / "src" / "main",
    scalaSource in Test := baseDirectory.value / "src" / "test",
    // watchSources += baseDirectory.value / "input",
    ivyLoggingLevel := UpdateLogging.Full,
    javaOptions += "-Xmx4G",
    shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " },
    shellPrompt := { state => System.getProperty("user.name") + "> " }
)

lazy val root = (project in file(".")).
  settings(personalSettings: _*).
  settings(preferredSettings: _*).
  settings(
    name := "scarab",
    // libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % Test,
    libraryDependencies ++= Seq (
      "commons-io" % "commons-io" % "2.4" % Compile,
      "javax.mail" % "mail" % "1.4.3" % Compile,
      "org.apache.httpcomponents" % "httpclient" % "4.3.5" % Compile,
      "org.jsoup" % "jsoup" % "1.8.1" % Compile,
      "com.typesafe" % "config" % "1.0.2" % Compile,
      "org.apache.commons" % "commons-email" % "1.3.1" % Compile
    ),
    mainClass in (Compile, packageBin) := mainapp,
    // mainClass in (Compile, assembly) := mainapp,
    mainClass in (Compile, run) := mainapp
    // mainClass in (Test, run) := maintest
  )
