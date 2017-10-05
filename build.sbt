lazy val auth0Version = "3.1.0"
lazy val codecVersion = "1.10"
lazy val configVersion = "1.3.1"
lazy val guavaVersion = "19.0"
lazy val jettyVersion = "9.4.6.v20170531"
lazy val json4sJacksonVersion = "3.5.1"
lazy val jtaVersion = "1.1"
lazy val junitVersion = "4.12"
lazy val logbackVersion = "1.2.3"
lazy val rxjavaVersion = "2.1.3"
lazy val scalatestVersion = "3.0.4"
lazy val scalatraVersion = "2.5.1"
lazy val servletVersion = "3.1.0"
lazy val slf4jVersion = "1.7.25"

lazy val buildSettings = Seq(
  organization := "org.mbari.m3",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.12.3")
)

lazy val consoleSettings = Seq(
  shellPrompt := { state =>
    val user = System.getProperty("user.name")
    user + "@" + Project.extract(state).currentRef.project + ":sbt> "
  },
  initialCommands in console :=
    """
      |import java.time.Instant
      |import java.util.UUID
    """.stripMargin
)

lazy val dependencySettings = Seq(
  libraryDependencies ++= {
    Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "ch.qos.logback" % "logback-core" % logbackVersion,
      "com.google.guava" % "guava" % guavaVersion,
      "com.typesafe" % "config" % configVersion,
      "junit" % "junit" % junitVersion % "test",
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion)
  },
  resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("releases"),
    "hohonuuli-bintray" at "http://dl.bintray.com/hohonuuli/maven")
)

lazy val optionSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8", // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    //"-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-value-discard",
    "-Xfuture"),
  javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

// --- Aliases
addCommandAlias("cleanall", ";clean;clean-files")

// --- Modules
lazy val appSettings = buildSettings ++ consoleSettings ++ dependencySettings ++
    optionSettings

val apps = Seq("jetty-main")

lazy val `panoptes` = (project in file("."))
  .enablePlugins(JettyPlugin)
  .settings(appSettings)
  .settings(
    name := "panoptes",
    version := "1.0-SNAPSHOT",
    fork := true,
    libraryDependencies ++= Seq(
        "com.auth0" % "java-jwt" % auth0Version,
        "commons-codec" % "commons-codec" % codecVersion,
        "io.reactivex.rxjava2" % "rxjava" % rxjavaVersion,
        "javax.servlet" % "javax.servlet-api" % servletVersion,
        "javax.transaction" % "jta" % jtaVersion,
        "org.json4s" %% "json4s-jackson" % json4sJacksonVersion,
        "org.eclipse.jetty" % "jetty-server" % jettyVersion % "compile;test",
        "org.eclipse.jetty" % "jetty-servlets" % jettyVersion % "compile;test",
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "compile;test",
        "org.scalatest" %% "scalatest" % scalatestVersion % "test",
        "org.scalatra" %% "scalatra" % scalatraVersion,
        "org.scalatra" %% "scalatra-json" % scalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % scalatraVersion,
        "org.scalatra" %% "scalatra-slf4j" % scalatraVersion,
        "org.scalatra" %% "scalatra-swagger" % scalatraVersion,
        "org.scalatra" %% "scalatra-swagger-ext" % scalatraVersion,
        "org.scalatra" %% "scalatra-scalatest" % scalatraVersion)
          .map(_.excludeAll(ExclusionRule("org.slf4j", "slf4j-jdk14"),
            ExclusionRule("org.slf4j", "slf4j-log4j12"),
            ExclusionRule("javax.servlet", "servlet-api"))),
    mainClass in assembly := Some("JettyMain")
  )
//  .settings( // config sbt-pack
//    xerial.sbt.Pack.packSettings ++ Seq(
//      packMain := Map("jetty-main" -> "JettyMain"),
//      packExtraClasspath := Map("jetty-main" -> Seq("${PROG_HOME}/conf")),
//      packJvmOpts := Map("jetty-main" -> Seq("-Duser.timezone=UTC", "-Xmx4g")),
//      packDuplicateJarStrategy := "latest",
//      packJarNameConvention := "original"
//    )
//  )

packMain := Map("jetty-main" -> "JettyMain")

packExtraClasspath := Map("jetty-main" -> Seq("${PROG_HOME}/conf"))

packJvmOpts := Map("jetty-main" -> Seq("-Duser.timezone=UTC", "-Xmx4g"))

packDuplicateJarStrategy := "latest"

packJarNameConvention := "original"


