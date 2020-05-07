package fof.daq.service.mysql.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import fof.daq.service.mysql.component.AbstractEntity

/**
 * 用户表
 *
 * 序列化时忽略密码和盐值
 * value = ["password", "password_salt"]
 * allowGetters = false (不可读)
 * allowSetters = true (可写入)
 * */
//@JsonIgnoreProperties(ignoreUnknown = true, value = ["pass_word", "pass_word_salt"], allowGetters = false, allowSetters = true)
@JsonNaming(PropertyNamingStrategy.LowerCaseStrategy::class)
data class User(
    var id: Long? = null,
    var user_name:String? = null,                // 用户名
    var pass_word:String? = null,                // 用户密码
    @field:JsonProperty("pass_word_salt") // 数据库字段映射
    var passwordSalt:String? = null,            // 密码校验盐值
    var mobile:String? = null,            // 用户手机号
    var email:String? = null,            // 用户手机号
    var ip:String? = null,            // IP
    var user_001:String? = null,            // 预留字段
    var user_002:String? = null,            // 预留字段
    var status: Int = Companion.StatusEnum.ALLOW.ordinal  // 状态
): AbstractEntity() {

    override fun tableName() = tableName

    companion object {
        /**
        * 表名
        * */
        const val tableName = "sys_user"

        /**
        * 状态枚举
        * */
        enum class StatusEnum(value: Int) {
           ALLOW(0), // 可用
           DENY(1)   // 禁止
        }
    }
}
