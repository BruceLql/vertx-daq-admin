package fof.daq.service.web.admin.record

import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.value
import fof.daq.service.mongo.model.CarrierReportInfoModel
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest
import java.nio.file.Files


/**
 *  raw ：原始数据文件下载
 */
@HandlerRequest(path = "/rawDownload", method = HttpMethod.GET)
class RawDownloadHandler @Autowired constructor(
    private val vertx: Vertx,
    private val carrierReportInfoModel: CarrierReportInfoModel
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/rawDownload==============")

        val json: HttpServerRequest = event.request()
        println("++++++++++++json:$json")
        val mobile = json.getParam("mobile")
        val taskId = json.getParam("taskId")
        mobile ?: throw IllegalArgumentException("缺少 mobile！")
        taskId ?: throw IllegalArgumentException("缺少 taskId！")
        val returnStr = JsonObject()
        carrierReportInfoModel.queryDetialData(mobile, taskId, "raw").subscribe({ it ->
            when {
                it.isEmpty() -> {
                    returnStr.put("status", "1").put("message", "未查询到数据!")
                    event.response().end(returnStr.toString())
                }
                else -> {
                    val resultStr = it[0].value<JsonObject>("result").toString()

                    val fileSystem = vertx.fileSystem()

                    val path = Files.createTempFile("$mobile-$taskId", ".txt")
                    println("path: $path")
                    Files.write(path, fof.daq.service.common.extension.GZIPUtils().compress(resultStr))

                    try {
                        event.response()
                            .putHeader(HttpHeaders.CONTENT_ENCODING, "gzip")
                            .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
                            .putHeader("Content-Disposition", "attachment;filename=${"$mobile-$taskId"}.txt")
                            .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                            .sendFile(path.toString())
                    }catch (e:Exception){
                        e.printStackTrace()
                    } finally {
                        fileSystem.delete(path.toString()) {
                            log.info("删除${path} ：${it.succeeded()}")
                        }
                    }
                }
            }

        }, {
            it.printStackTrace()
        })

    }

}
