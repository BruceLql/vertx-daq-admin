package fof.daq.service.web.admin.record

import fof.daq.service.common.extension.error
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.value
import fof.daq.service.mongo.model.CarrierLoggingEventModel
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  采集列表展示 - 按运营商类型、地区 分页查询
 */
@HandlerRequest(path = "/logListPage", method = HttpMethod.POST)
class LoggingListPageHandler @Autowired constructor(
    private val carrierLoggingEventModel: CarrierLoggingEventModel
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/logListPage==============")
        try {
            val json = event.bodyAsJson
            val mobile = json.value<String>("mobile")
            val level = json.value<String>("level")
//            val startTime = json.value<String>("startTime") ?: throw IllegalArgumentException("startTime 参数不合法！")
//            val endTime = json.value<String>("endTime") ?: throw IllegalArgumentException("endTime 参数不合法！")
//            if (!regexDate(startTime)) throw IllegalArgumentException("startTime 不符合时间格式！")
//            if (!regexDate(endTime)) throw IllegalArgumentException("endTime 不符合时间格式！")
            val currentPage = json.value<Int>("currentPage") ?: throw IllegalArgumentException("currentPage 参数不合法！")
            val size = json.value<Int>("size") ?: throw IllegalArgumentException("size 参数不合法！")
            val query = JsonObject()
            if (!mobile.isNullOrEmpty()) {
                query.put("mobile", mobile)
            }
            if (!level.isNullOrEmpty()) {
                query.put("level", level)
            }

            carrierLoggingEventModel.listPageData(query, currentPage, size).subscribe({ list ->

                log.info("=======采集列表 按运营商类型、地区 分页查询===========: ${list.toJson()}")
                event.response().end(list.toJson().toString())

            }, { it.printStackTrace() })

        } catch (e: Exception) {
            e.printStackTrace()
            event.response().error(e, 500, message = "异常，请联系管理员排查")
        }

    }
}
