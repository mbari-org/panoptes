lazy val auth0Version         = "3.19.4"
lazy val circeVersion         = "0.14.3"
lazy val codecVersion         = "1.15"
lazy val configVersion        = "1.4.2"
lazy val jettyVersion         = "9.4.50.v20221201"
lazy val json4sJacksonVersion = "4.0.6"
lazy val jansiVersion         = "2.4.0"
lazy val jtaVersion           = "1.1"
lazy val junitVersion         = "4.13.2"
lazy val logbackVersion       = "1.4.5"
lazy val rxjavaVersion        = "3.1.6"
lazy val scalatestVersion     = "3.2.15"
lazy val scalatraVersion      = "3.0.0-M3"
lazy val servletVersion       = "4.0.1"
lazy val slf4jVersion         = "2.0.6"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val buildSettings = Seq(
  organization := "org.mbari.m3",
  scalaVersion := "3.2.2",
  organizationName := "Monterey Bay Aquarium Research Institute",
  startYear := Some(2017),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
)


lazy val dependencySettings = Seq(
  libraryDependencies ++= {
    Seq(
      "ch.qos.logback" % "logback-classic"  % logbackVersion,
      "ch.qos.logback" % "logback-core"     % logbackVersion,
      "com.typesafe"   % "config"           % configVersion,
      "junit"          % "junit"            % junitVersion % "test",
      "org.slf4j"      % "log4j-over-slf4j" % slf4jVersion,
      "org.slf4j"      % "slf4j-api"        % slf4jVersion
    )
  },
  resolvers ++= Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("releases"),
  )
)

lazy val optionSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8", // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:implicitConversions",
    "-unchecked",
    "-Xlint",
    "-Ywarn-value-discard"
  ),
  javacOptions ++= Seq("-target", "17", "-source", "17"),
  updateOptions := updateOptions.value.withCachedResolution(true)
)

// --- Aliases
addCommandAlias("cleanall", ";clean;clean-files")

// --- Modules
lazy val appSettings = buildSettings ++ dependencySettings ++
  optionSettings

lazy val apps = Map("jetty-main" -> "JettyMain") // for sbt-pack

lazy val `panoptes` = (project in file("."))
  .enablePlugins(
    AutomateHeaderPlugin, 
    GitBranchPrompt, 
    GitVersioning,
    JettyPlugin, 
    PackPlugin
  )
  .settings(appSettings)
  .settings(
    name := "panoptes",
    fork := true,
    // Set version based on git tag. I use "0.0.0" format (no leading "v", which is the default)
    // Use `show gitCurrentTags` in sbt to update/see the tags
    git.gitTagToVersionNumber := { tag: String =>
      if(tag matches "[0-9]+\\..*") Some(tag)
      else None
    },
    git.useGitDescribe := true,
    libraryDependencies ++= Seq(
      "com.auth0"            % "java-jwt"            % auth0Version,
      "commons-codec"        % "commons-codec"       % codecVersion,
      "io.reactivex.rxjava3" % "rxjava"              % rxjavaVersion,
      "javax.servlet"        % "javax.servlet-api"   % servletVersion,
      "javax.transaction"    % "jta"                 % jtaVersion,
      "io.circe"             %% "circe-core"         % circeVersion,
      "io.circe"             %% "circe-generic"      % circeVersion,
      "io.circe"             %% "circe-parser"       % circeVersion,
      "org.fusesource.jansi" % "jansi"               % jansiVersion % "runtime",
      ("org.json4s"           %% "json4s-jackson"     % json4sJacksonVersion).cross(CrossVersion.for3Use2_13),
      "org.eclipse.jetty"    % "jetty-server"        % jettyVersion % "compile;test",
      "org.eclipse.jetty"    % "jetty-servlets"      % jettyVersion % "compile;test",
      "org.eclipse.jetty"    % "jetty-webapp"        % jettyVersion % "compile;test",
      ("org.scalatest"        %% "scalatest"          % scalatestVersion).cross(CrossVersion.for3Use2_13) % "test",
      ("org.scalatra"         %% "scalatra"           % scalatraVersion).cross(CrossVersion.for3Use2_13),
      ("org.scalatra"         %% "scalatra-json"      % scalatraVersion).cross(CrossVersion.for3Use2_13),
      // "org.scalatra"         %% "scalatra-scalate"   % scalatraVersion,
      ("org.scalatra"         %% "scalatra-scalatest" % scalatraVersion).cross(CrossVersion.for3Use2_13)
    ).map(
      _.excludeAll(
        ExclusionRule("org.slf4j", "slf4j-jdk14"),
        ExclusionRule("org.slf4j", "slf4j-log4j12"),
        ExclusionRule("javax.servlet", "servlet-api")
      )
    )
  )
  .settings( // config sbt-pack
    packMain := apps,
    packExtraClasspath := apps.keys.map(k => k -> Seq("${PROG_HOME}/conf")).toMap,
    packJvmOpts := apps.keys.map(k => k        -> Seq("-Duser.timezone=UTC", "-Xmx4g")).toMap,
    packDuplicateJarStrategy := "latest",
    packJarNameConvention := "original"
  )
