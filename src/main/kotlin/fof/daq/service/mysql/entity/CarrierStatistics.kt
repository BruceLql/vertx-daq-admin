package fof.daq.service.mysql.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import fof.daq.service.mysql.component.AbstractEntity

/**
 *  采集任务执行情况汇总
 *
 * 序列化时忽略密码和盐值
 * value = ["password", "password_salt"]
 * allowGetters = false (不可读)
 * allowSetters = true (可写入)
 * */
@JsonIgnoreProperties(ignoreUnknown = true, value = [], allowGetters = false, allowSetters = true)
@JsonNaming(PropertyNamingStrategy.LowerCaseStrategy::class)
data class CarrierStatistics(
    var id: Long? = null,
    var mobile: String? = null,             // 手机号
    var operator: String? = null,           // 运营商类型
    var statistics: Int? = 0,               // 执行时间（毫秒数/ms）
    var city: String? = null,               // 城市
    var province: String? = null,           // 省份
    var ip: String? = null,                 // IP
    @field:JsonProperty("app_name")
    var app_name: String? = null,            // 服务名
    var sucess: Boolean? = false,           // 城市
    var carrier_001: String? = null,        // 预留字段
    var carrier_002: String? = null         // 预留字段
) : AbstractEntity() {

    override fun tableName() = tableName

    companion object {
        /**
         * 表名
         * */
        const val tableName = "carrier_statistics"

    }
}
