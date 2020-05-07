package fof.daq.service.web.admin.record

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.HttpStatusException
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.toEntity
import fof.daq.service.common.extension.value
import fof.daq.service.mongo.model.RecordModel
import fof.daq.service.mysql.dao.SysConfigDao
import fof.daq.service.mysql.entity.SysConfig
import fof.daq.service.mysql.entity.User
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  系统配置相关参数查询接口：
 *      1：失败通知次数 notice_cnt
 *      2：缓存时间  cache_time
 *      3：回调地址  callBackUrl
 */
@HandlerRequest(path = "/configEdit", method = HttpMethod.POST)
class SysConfigEditHandler @Autowired constructor(
    private val vertx: Vertx,
    private val sysConfigDao: SysConfigDao
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/configEdit==============")
        val sysConfig = try {
            val json = event.bodyAsJson
            json.value<String>("configKey")?: throw IllegalArgumentException("缺少key！")
            json.value<String>("configValue")?: throw IllegalArgumentException("缺少value！")
            event.bodyAsJson.toEntity<SysConfig>()
        } catch (e: DecodeException) {
            event.fail(HttpStatusException(400, e))
            return
        }

        sysConfigDao.editSysConfig(sysConfig).subscribe({ result ->

            log.info("result : ${result.toJson()}")

            event.response().end(JsonObject().put("updated",result.updated).toString())

        }, {
            it.printStackTrace()
            event.response().end()
        })

    }
}
