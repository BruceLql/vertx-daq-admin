package fof.daq.service.web.admin.record

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.toEntity
import fof.daq.service.common.extension.value
import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.model.CarrierReportInfoModel
import fof.daq.service.mongo.model.RecordModel
import fof.daq.service.mysql.dao.SysConfigDao
import fof.daq.service.mysql.entity.SysConfig
import fof.daq.service.mysql.entity.User
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  运营商数据列表展示，按时间降序
 *      raw:  原始数据文件下载
 *      report: 报告数据展示
 */
@HandlerRequest(path = "/recordHisoryList", method = HttpMethod.POST)
class RecordHistoryHandler @Autowired constructor(
    private val vertx: Vertx,
    private val carrierReportInfoModel: CarrierReportInfoModel
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/recordHisoryList==============")

        val json = event.bodyAsJson
        val mobile = json.value<String>("mobile")
        val page = json.value<Int>("page") ?: throw IllegalArgumentException("缺少当前页参数 page！")
        val size = json.value<Int>("size") ?: throw IllegalArgumentException("缺少每页条数参数 size！")
        val query = JsonObject()
        if (!mobile.isNullOrEmpty()) {
            query.put("mobile", mobile)
        }

        vertx.executeBlocking<String>({
            carrierReportInfoModel.queryHistoryListResultData(query, page, size).subscribe({ result ->

                log.info("result : ${result.toJson()}")
                it.complete(result.toJson().toString())
            }, {
                it.printStackTrace()
                event.response().end()
            })
        }, {
            event.response().end(it.result())
        })

    }
}
