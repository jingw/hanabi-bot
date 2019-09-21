lazy val root = (project in file(".")).
  settings(
    scalaVersion := "2.13.1",
    scalacOptions ++= Seq(
      "-deprecation", "-unchecked", "-feature",
      "-Werror",
      "-target:jvm-1.8"
    ),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test"
    ),
    name := "hanabi-bot",
    description := "Bot that plays Hanabi"
  )
