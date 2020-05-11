package fof.daq.service.mongo.schema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import fof.daq.service.mongo.component.AbstractSchema
import io.vertx.core.json.JsonArray


/**
 * CarrierPyServer 服务器列表
 */
//忽略该目标对象不存在的属性
@JsonIgnoreProperties(ignoreUnknown = true)
data class CarrierPyServer(

    var host: String? = null, // IP
    var port: String? = null, // 端口
    var server_name: String? = null, // 服务名称
    var heart_beat_time: Long? = 0, //  时间戳
    var tags: JsonArray? = null, // 标签
    var version: String? = null, // 版本
    var status: Int = ServerState.DOWN.code, // 状态 1：可用 2：不可用 3：下线
    var switch: Int = ServerSwitch.DISABLE.code  // 开通状态：1 ：开通 0 ：关闭

) : AbstractSchema() {
    override fun tableName() = TABLE_NAME

    // 静态方法 属性
    companion object {
        const val TABLE_NAME = "py_server"

        /**
         * 服务状态
         */
        enum class ServerState(var code: Int, var content: String) {
            UP(1, "可用"),
            UNAVAILABLE(2, "不可用"),
            DOWN(3, "下线")
        }

        /**
         * 服务开通状态
         */
        enum class ServerSwitch(var code: Int, var content: String) {
            ENABLE(1, "开通"),
            DISABLE(0, "关闭")
        }
    }


}
