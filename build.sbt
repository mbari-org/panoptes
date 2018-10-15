lazy val auth0Version = "3.4.0"
lazy val codecVersion = "1.11"
lazy val configVersion = "1.3.3"
lazy val jettyVersion = "9.4.12.v20180830"
lazy val json4sJacksonVersion = "3.6.1"
lazy val jtaVersion = "1.1"
lazy val junitVersion = "4.12"
lazy val logbackVersion = "1.2.3"
lazy val rxjavaVersion = "2.2.2"
lazy val scalatestVersion = "3.0.5"
lazy val scalatraVersion = "2.6.3"
lazy val servletVersion = "3.1.0"
lazy val slf4jVersion = "1.7.25"

lazy val buildSettings = Seq(
  organization := "org.mbari.m3",
  scalaVersion := "2.12.7",
  crossScalaVersions := Seq("2.12.7"),
  organizationName := "Monterey Bay Aquarium Research Institute",
  startYear := Some(2017),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
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
      "com.typesafe" % "config" % configVersion,
      "junit" % "junit" % junitVersion % "test",
      "org.scalatest" %% "scalatest" % scalatestVersion % "test",
      "org.slf4j" % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion
    )
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
    "-Xfuture"
  ),
  javacOptions ++= Seq("-target", "1.8", "-source", "1.8"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

// --- Aliases
addCommandAlias("cleanall", ";clean;clean-files")

// --- Modules
lazy val appSettings = buildSettings ++ consoleSettings ++ dependencySettings ++
  optionSettings

lazy val apps = Map("jetty-main" -> "JettyMain")  // for sbt-pack

lazy val `panoptes` = (project in file("."))
  .enablePlugins(JettyPlugin)
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(PackPlugin)
  .settings(appSettings)
  .settings(
    name := "panoptes",
    version := "0.1.0",
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
      "org.scalatra" %% "scalatra-scalatest" % scalatraVersion
    ).map(
        _.excludeAll(
          ExclusionRule("org.slf4j", "slf4j-jdk14"),
          ExclusionRule("org.slf4j", "slf4j-log4j12"),
          ExclusionRule("javax.servlet", "servlet-api"))),
    mainClass in assembly := Some("JettyMain")
  )
  .settings( // config sbt-pack
    packMain := apps,
    packExtraClasspath := apps.keys.map(k => k -> Seq("${PROG_HOME}/conf")).toMap,
    packJvmOpts := apps.keys.map(k => k -> Seq("-Duser.timezone=UTC", "-Xmx4g")).toMap,
    packDuplicateJarStrategy := "latest",
    packJarNameConvention := "original"
  )
