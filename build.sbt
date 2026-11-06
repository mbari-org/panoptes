import Dependencies._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / javacOptions ++= Seq("-target", "21", "-source", "21")
ThisBuild / licenses         := Seq("Apache-2.0" -> uri("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / organization     := "org.mbari"
ThisBuild / organizationName := "Monterey Bay Aquarium Research Institute"
ThisBuild / scalaVersion     := "3.10.0"
ThisBuild / usePipelining    := true
ThisBuild / scalacOptions ++= Seq(
    "-deprecation",  // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8",         // yes, this is 2 args. Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature",      // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Ywarn-value-discard"
)
ThisBuild / startYear        := Some(2017)
ThisBuild / versionScheme    := Some("semver-spec")
ThisBuild / Test / fork              := true
ThisBuild / Test / parallelExecution := false
ThisBuild / Test / testOptions += Tests.Argument(TestFrameworks.MUnit, "-b")
ThisBuild / Test / javaOptions ++= Seq(
    "-Duser.timezone=UTC"
)


lazy val panoptes = (project in file("."))
  .enablePlugins(
    AutomateHeaderPlugin, 
    GitBranchPrompt, 
    GitVersioning,
    JavaAppPackaging
  )
  .settings(
    name := "panoptes",
    fork := true,
    // Set version based on git tag. I use "0.0.0" format (no leading "v", which is the default)
    // Use `show gitCurrentTags` in sbt to update/see the tags
    git.gitTagToVersionNumber := { (gitTag: String) =>
        if (gitTag.matches("[0-9]+\\..*")) Some(gitTag)
        else None
    },
    git.useGitDescribe := true,
    // sbt 2.x moves `target` under target/out/jvm/scala-<ver>/<project>. Pin the staging
    // directory to its sbt 1.x location so the Dockerfile's COPY target/universal/stage still works.
    Universal / com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stagingDirectory := baseDirectory.value / "target" / "universal" / "stage",
    bashScriptExtraDefines ++= Seq(
            """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
            """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
        ),
    batScriptExtraDefines ++= Seq(
        """call :add_java "-Dconfig.file=%APP_HOME%\conf\application.conf"""",
        """call :add_java "-Dlogback.configurationFile=%APP_HOME%\conf\logback.xml""""
    ),
    libraryDependencies ++= Seq(
      auth0,
      circeCore,
      circeGeneric,
      circeParser,
      commonsCodec,
      jansi % "runtime",
      junit % "test",
      logback % "runtime,test",
      munit % "test",
      rxjava,
      slf4jJulBridge % "runtime,test",
      slf4jSystem % "runtime,test",
      tapirCirce,
      tapirPrometheus,
      tapirServerStub % "test",
      tapirSttpCirce,
      tapirSwagger,
      tapirVertex,
      typesafeConfig
    )
  )
