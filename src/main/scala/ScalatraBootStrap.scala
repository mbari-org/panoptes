import java.util.concurrent.Executors
import javax.servlet.ServletContext

import org.mbari.m3.panoptes.api.ImageV1Api
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-20T14:41:00
 */
class ScalatraBootstrap extends LifeCycle {

  private[this] val log = LoggerFactory.getLogger(getClass)

  override def init(context: ServletContext): Unit = {

    log.info("STARTING UP NOW")

    implicit val executionContext = ExecutionContext.global
    //implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

    //val helloApi = new api.HelloApi
    val imageV1Api = new ImageV1Api()

    context.mount(imageV1Api, "/v1/images")

  }

}
