lazy val root = (project in file(".")).
  settings(jacoco.settings).
  settings(
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-deprecation", "-unchecked", "-feature",
      "-Xfatal-warnings",
      "-target:jvm-1.8"
    ),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    resolvers ++= Seq(
      "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % Test
    ),
    name := "hanabi-bot",
    description := "Bot that plays Hanabi",
    version := "0.1",
    jacoco.reportFormats in jacoco.Config := Seq(
      de.johoop.jacoco4sbt.XMLReport(),
      de.johoop.jacoco4sbt.ScalaHTMLReport(withBranchCoverage = true)
    )
  )
