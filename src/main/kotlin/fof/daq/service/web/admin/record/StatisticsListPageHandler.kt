package fof.daq.service.web.admin.record

import fof.daq.service.common.extension.error
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.regexDate
import fof.daq.service.common.extension.value
import fof.daq.service.service.CarrierStatisticsService
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  采集列表展示 - 按运营商类型、地区 分页查询
 */
@HandlerRequest(path = "/statisticsListPage", method = HttpMethod.POST)
class StatisticsListPageHandler @Autowired constructor(
    private val vertx: Vertx,
    private val carrierStatisticsService: CarrierStatisticsService
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/statisticsListPage==============")
        try {
            val json = event.bodyAsJson
            val startTime = json.value<String>("startTime")?:throw IllegalArgumentException("startTime 参数不合法！")
            val endTime = json.value<String>("endTime")?:throw IllegalArgumentException("endTime 参数不合法！")
            val currentPage = json.value<Int>("currentPage")?:throw IllegalArgumentException("currentPage 参数不合法！")
            val size = json.value<Int>("size")?:throw IllegalArgumentException("size 参数不合法！")
            if (!regexDate(startTime)) throw IllegalArgumentException("startTime 不符合时间格式！")
            if (!regexDate(endTime)) throw IllegalArgumentException("endTime 不符合时间格式！")
            vertx.executeBlocking<String>({
                carrierStatisticsService.queryGroupByOperatorAndAreaListPage(startTime, endTime,currentPage,size).subscribe({ list ->

                    log.info("=======采集列表 按运营商类型、地区 分页查询===========: ${list.toJson()}")
                    it.complete(list.toJson().toString())
                },{it.printStackTrace()})
            },{
                event.response().end(it.result())
            })

        }catch (e:Exception){
            e.printStackTrace()
            event.response().error(e,500,message = "异常，请联系管理员排查")
        }

    }
}
