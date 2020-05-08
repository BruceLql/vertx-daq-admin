package fof.daq.service.web.admin.record

import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.toEntity
import fof.daq.service.common.extension.value
import fof.daq.service.mysql.entity.CarrierStatistics
import fof.daq.service.service.CarrierStatisticsService
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.HttpStatusException
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  采集情况记录  存储到mysql
 */
@HandlerRequest(path = "/saveStatistics", method = HttpMethod.POST)
class SaveStatisticsHandler @Autowired constructor(
    private val carrierStatisticsService: CarrierStatisticsService
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/saveStatistics==============")

        val json = event.bodyAsJson
        val mobile = json.value<String>("mobile") ?: throw IllegalArgumentException("缺少参数 mobile！")
        val operator = json.value<String>("operator") ?: throw IllegalArgumentException("缺少参数 operator！")
        val statistics = json.value<Int>("statistics") ?: throw IllegalArgumentException("缺少参数 statistics！")
        val city = json.value<String>("city") ?: throw IllegalArgumentException("缺少参数 city！")
        val province = json.value<String>("province") ?: throw IllegalArgumentException("缺少参数 province！")
        val ip = json.value<String>("ip") ?: throw IllegalArgumentException("缺少参数 ip！")
        val appName = json.value<String>("app_name") ?: throw IllegalArgumentException("缺少参数 appName！")
        val sucess = json.value<Boolean>("sucess") ?: throw IllegalArgumentException("缺少参数 sucess！")

        val carrierStatistics = try {
            event.bodyAsJson.toEntity<CarrierStatistics>()
        } catch (e: DecodeException) {
            event.fail(HttpStatusException(400, e))
            return
        }
        carrierStatisticsService.dataInsertToMysql(carrierStatistics)?.subscribe({ result ->

            log.info("采集情况记录  存储到mysql 返回result : ${result.toJson()}")

            event.response().end(JsonObject().put("code","0000").put("message","success").toString())

        }, {
            it.printStackTrace()
            event.response().end(JsonObject().put("code","1111").put("message","error").toString())
        })

    }
}
