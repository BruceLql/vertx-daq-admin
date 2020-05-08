package fof.daq.service.mongo.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fof.daq.service.mongo.component.AbstractSchema


/**
 * CarrierStatistics 爬虫任务执行情况统计
 */
//忽略该目标对象不存在的属性
@JsonIgnoreProperties(ignoreUnknown = true)
data class CarrierLoggingEvent constructor(

        var mobile: String? = null, // 手机号
        var record_time: String? = null, // 时间戳
        var level: String? = null, // 日志级别 INFO
        var ip: String? = null, //  IP地址
        var content: String? = null, // 内容
        var session_id: Long? = 0, // sessionID
        var app_name: String? = null, // 产品名称
        var port: String ? = null , // 端口
        var version: String ? = null  // 版本

) : AbstractSchema() {
    override fun tableName() = TABLE_NAME
    // 静态方法 属性
    companion object {
        const val TABLE_NAME = "logging_event"
    }
}
