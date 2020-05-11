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
@HandlerRequest(path = "/pyServerEditSwitch", method = HttpMethod.POST)
class pyServerEditSwitchHandler @Autowired constructor(
    private val vertx: Vertx,
    private val carrierPyServerService: CarrierPyServerService
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/pyServerEditSwitch==============")
        try {
            val json = event.bodyAsJson
            val _id = json.value<String>("_id") ?: throw IllegalArgumentException("_id 参数不合法！")
            val switch = json.value<Int>("switch") ?: throw IllegalArgumentException("switch 参数不合法！")

            val query = JsonObject().put("_id", _id).put("switch", switch)

            carrierPyServerService.updateSwitchById(query).subscribe({
                println(it.toString())
                val value = it.value<Long>("doc_modified")?:0

                event.response().end(JsonObject().put("modified",value).toString())

            }, { it.printStackTrace() })

        } catch (e: Exception) {
            e.printStackTrace()
            event.response().error(e, 500, message = "异常，请联系管理员排查")
        }

    }
}
