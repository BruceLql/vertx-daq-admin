package fof.daq.service.web.admin.record

import fof.daq.service.common.extension.error
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.regexDate
import fof.daq.service.common.extension.value
import fof.daq.service.service.CarrierStatisticsService
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest
import java.util.*

/**
 *  采集列表展示 - 合计统计信息
 */
@HandlerRequest(path = "/statisticsList", method = HttpMethod.POST)
class StatisticsListHandler @Autowired constructor(
    private val carrierStatisticsService: CarrierStatisticsService
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/statisticsList==============")
        try {
            val json = event.bodyAsJson
            val startTime = json.value<String>("startTime")?:throw IllegalArgumentException("startTime 参数不合法！")
            val endTime = json.value<String>("endTime")?:throw IllegalArgumentException("endTime 参数不合法！")
            if (!regexDate(startTime)) throw IllegalArgumentException("startTime 不符合时间格式！")
            if (!regexDate(endTime)) throw IllegalArgumentException("endTime 不符合时间格式！")

            carrierStatisticsService.queryTotal(startTime, endTime).subscribe({ list ->
                val totalList = ArrayList<JsonObject>()
                // 取第一组 第一个
                totalList.add(list[0][0])
                // 第二组 循环取出
                list[1].forEach{
                    totalList.add(it)
                }
                log.info("=======采集列表汇总统计数据===========: $totalList")
                event.response().end(totalList.toString())

            },{it.printStackTrace()})

        }catch (e:Exception){
            e.printStackTrace()
            event.response().error(e,500,message = "异常，请联系管理员排查")
        }

    }
}
