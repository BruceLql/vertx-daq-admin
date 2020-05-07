package fof.daq.service.web.admin.record

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import fof.daq.service.common.extension.logger
import fof.daq.service.mysql.dao.SysConfigDao
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

/**
 *  系统配置相关参数查询接口：
 *      1：失败通知次数 notice_cnt int
 *      2：缓存时间  cache_time
 *      3：回调地址  callBackUrl
 */
@HandlerRequest(path = "/configList", method = HttpMethod.GET)
class SysConfigHandler @Autowired constructor(
    private val sysConfigDao: SysConfigDao
) : ControllerHandler() {
    private val log = logger(this::class)
    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        log.info("=========/record/configList==============")
        sysConfigDao.querySysConfigList().subscribe({ confList ->

            log.info("查询出来的配置参数：$confList")
            event.response().end(confList.toString())

        }, { it.printStackTrace() })

    }
}
