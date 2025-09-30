import sbt.*
object Dependencies {

    lazy val auth0 = "com.auth0" % "java-jwt" % "4.5.0"


    val circeVersion      = "0.14.15"
    lazy val circeCore    = "io.circe" %% "circe-core"    % circeVersion
    lazy val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    lazy val circeParser  = "io.circe" %% "circe-parser"  % circeVersion

    lazy val commonsCodec = "commons-codec" % "commons-codec" % "1.18.0"

    lazy val jansi       = "org.fusesource.jansi"    % "jansi"                      % "2.4.1"
    lazy val junit       = "junit"                   % "junit"                      % "4.13.2"
    lazy val logback     = "ch.qos.logback"          % "logback-classic"            % "1.5.16"
    lazy val munit       = "org.scalameta"          %% "munit"                      % "1.2.0"
    lazy val rxjava      = "io.reactivex.rxjava3"    % "rxjava"                     % "3.1.10"

    val slf4jVersion = "2.0.16"
    lazy val slf4jJulBridge = "org.slf4j" % "jul-to-slf4j"               % slf4jVersion
    lazy val slf4jSystem    = "org.slf4j" % "slf4j-jdk-platform-logging" % slf4jVersion

    private val tapirVersion = "1.11.14"
    lazy val tapirCirce      = "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion
    lazy val tapirPrometheus = "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion
    lazy val tapirServerStub = "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % tapirVersion
    lazy val tapirSwagger    = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % tapirVersion
    lazy val tapirVertex     = "com.softwaremill.sttp.tapir" %% "tapir-vertx-server"       % tapirVersion

    lazy val tapirSttpCirce = "com.softwaremill.sttp.client3" %% "circe" % "3.11.0"

    lazy val typesafeConfig = "com.typesafe"    % "config"     % "1.4.3"


}
