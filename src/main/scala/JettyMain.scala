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
import org.scalatra.servlet.ScalatraListener
import org.slf4j.LoggerFactory
import org.mbari.m3.panoptes.AppConfig
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.util.resource.Resource
import org.eclipse.jetty.util.resource.PathResource
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.util.resource.ResourceFactory

object JettyMain {

  object conf {
    private val config = ConfigFactory.load()
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


    // -- Configure Server
    val server: Server = new Server
    LoggerFactory
      .getLogger(getClass)
      .atInfo
      .log("Starting Jetty server on port {}", conf.port)

    server.setStopTimeout(conf.stopTimeout)
    server.setStopAtShutdown(true)

    // -- Add Request logging as NCSA extended format
    val logWriter = new Slf4jRequestLogWriter
    logWriter.setLoggerName("jetty.request")
    val requestLog = new CustomRequestLog(logWriter, CustomRequestLog.EXTENDED_NCSA_FORMAT)
    server.setRequestLog(requestLog)

    // -- Configure HTTP
    val httpConfig = new HttpConfiguration()
    httpConfig.setSendDateHeader(true)
    httpConfig.setSendServerVersion(false)

    val connector = new NetworkTrafficServerConnector(server, new HttpConnectionFactory(httpConfig))
    connector.setPort(conf.port)
    connector.setIdleTimeout(conf.connectorIdleTimeout)
    server.addConnector(connector)

    // -- Configure Servlets
    val webApp = new WebAppContext
    webApp.setContextPath(conf.contextPath)

    // https://jetty.org/docs/jetty/12/programming-guide/server/http.html#handler-use
    val handler = new ResourceHandler();
    webApp.setBaseResource(ResourceFactory.of(handler).newResource(conf.webapp))
    handler.setDirAllowed(false);
    // webApp.setResourceBase(conf.webapp)
    webApp.setEventListeners(java.util.List.of(new ScalatraListener))
    server.setHandler(webApp)

    // -- GO!
    server.start()
  }
}
