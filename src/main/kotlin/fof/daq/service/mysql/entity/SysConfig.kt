package fof.daq.service.mysql.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import fof.daq.service.mysql.component.AbstractEntity

/**
 * 系统参数配置表
 *
 * 序列化时忽略密码和盐值
 * value = ["password", "password_salt"]
 * allowGetters = false (不可读)
 * allowSetters = true (可写入)
 * */
@JsonIgnoreProperties(ignoreUnknown = true, value = [], allowGetters = false, allowSetters = true)
@JsonNaming(PropertyNamingStrategy.LowerCaseStrategy::class)
data class SysConfig(
    var id: Long? = null,
//    @field:JsonProperty("config_key")
    var configKey: String? = null,                // 配置项名称
//    @field:JsonProperty("config_value")
    var configValue: String? = null,              // 配置项value
    var note: String? = null,             // 备注，参数功能描述
    var status: String? = null           // 0:可用；1：禁用

) : AbstractEntity() {

    override fun tableName() = tableName

    companion object {
        /**
         * 表名
         * */
        const val tableName = "sys_config"

    }
}
