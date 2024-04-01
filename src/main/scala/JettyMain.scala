/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server._
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.slf4j.LoggerFactory
import org.mbari.m3.panoptes.AppConfig

object JettyMain {

  object conf {
    private[this] val config = ConfigFactory.load()
    val port = config.getInt("http.port")
    val stopTimeout = config.getInt("http.stop.timeout")
    val connectorIdleTimeout = config.getInt("http.connector.idle.timeout")
    val webapp = config.getString("http.webapp")
    val contextPath = config.getString("http.context.path")
  }

  def main(args: Array[String]) = {
    System.setProperty("user.timezone", "UTC")
    val s = """
      |  _______  _______  _        _______  _______ _________ _______  _______ 
      | (  ____ )(  ___  )( (    /|(  ___  )(  ____ )\__   __/(  ____ \(  ____ \
      | | (    )|| (   ) ||  \  ( || (   ) || (    )|   ) (   | (    \/| (    \/
      | | (____)|| (___) ||   \ | || |   | || (____)|   | |   | (__    | (_____ 
      | |  _____)|  ___  || (\ \) || |   | ||  _____)   | |   |  __)   (_____  )
      | | (      | (   ) || | \   || |   | || (         | |   | (            ) |
      | | )      | )   ( || )  \  || (___) || )         | |   | (____/\/\____) |
      | |/       |/     \||/    )_)(_______)|/          )_(   (_______/\_______)""".stripMargin + s"  v${AppConfig.Version}"

    println(s)


    val server: Server = new Server
    LoggerFactory
      .getLogger(getClass)
      .atInfo
      .log("Starting Jetty server on port {}", conf.port)

    server setStopTimeout conf.stopTimeout
    //server setDumpAfterStart true
    server setStopAtShutdown true

    val httpConfig = new HttpConfiguration()
    httpConfig setSendDateHeader true
    httpConfig setSendServerVersion false

    val connector = new NetworkTrafficServerConnector(server, new HttpConnectionFactory(httpConfig))
    connector setPort conf.port
    //connector setSoLingerTime 0
    connector setIdleTimeout conf.connectorIdleTimeout
    server addConnector connector

    // val webapp = conf.webapp
    val webApp = new WebAppContext
    webApp setContextPath conf.contextPath
    webApp setResourceBase conf.webapp
    // webApp setEventListeners Array(new ScalatraListener)
    webApp.setEventListeners(java.util.List.of(new ScalatraListener))

    server setHandler webApp

    server.start()
  }
}
