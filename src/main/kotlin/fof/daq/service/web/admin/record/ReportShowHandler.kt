package fof.daq.service.web.admin.record

import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.value
import fof.daq.service.mongo.model.CarrierReportInfoModel
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *    report: 报告数据展示
 */
@HandlerRequest(path = "/reportShow", method = HttpMethod.GET)
class ReportShowHandler @Autowired constructor(
    private val carrierReportInfoModel: CarrierReportInfoModel
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/reportShow==============")

        val json = event.request()
        println("++++++++++++json:$json")
        val mobile = json.getParam("mobile")
        val taskId = json.getParam("taskId")
        mobile ?: throw IllegalArgumentException("缺少 mobile！")
        taskId ?: throw IllegalArgumentException("缺少 taskId！")
        val returnStr = JsonObject()
        carrierReportInfoModel.queryDetialData(mobile, taskId, "report").subscribe({ it ->
            when {
                it.isEmpty() -> {
                    returnStr.put("status", "1").put("message", "未查询到数据!")
                    event.response().putHeader("content-type", "application/json; charset=utf-8").end(returnStr.toString())
                }
                else -> {
                    val resultStr = it[0].value<JsonObject>("result").toString()
//                    val compress = GZIPUtils().compress(resultStr)

                    try {

                        event.response().putHeader("content-type", "application/json; charset=utf-8").end(resultStr)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }, {
            it.printStackTrace()
        })

    }
}
