package fof.daq.service.web.admin.record

import fof.daq.service.common.extension.error
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.value
import fof.daq.service.service.CarrierPyServerService
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  采集列表展示 - 按运营商类型、地区 分页查询
 */
@HandlerRequest(path = "/pyServerListPage", method = HttpMethod.POST)
class pyServerListPageHandler @Autowired constructor(
    private val vertx: Vertx,
    private val carrierPyServerService: CarrierPyServerService
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/pyServerListPage==============")
        try {
            val json = event.bodyAsJson
            val status = json.value<Int>("status")
            val switch = json.value<Int>("switch")
            val currentPage = json.value<Int>("currentPage") ?: throw IllegalArgumentException("currentPage 参数不合法！")
            val size = json.value<Int>("size") ?: throw IllegalArgumentException("size 参数不合法！")
            val query = JsonObject()
            if (status != null) {
                query.put("status", status)
            }
            if (switch != null) {
                query.put("switch", switch)
            }
            carrierPyServerService.pyServerList(query, currentPage, size).subscribe({
                event.response().end(it.toJson().toString())
            }, { it.printStackTrace() })

        } catch (e: Exception) {
            e.printStackTrace()
            event.response().error(e, 500, message = "异常，请联系管理员排查")
        }

    }
}
