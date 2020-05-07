package fof.daq.service.mysql.dao

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.sql.UpdateResult
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import fof.daq.service.common.extension.logger
import fof.daq.service.mysql.component.AbstractDao
import fof.daq.service.mysql.component.SQL
import fof.daq.service.mysql.entity.SysConfig
import fof.daq.service.mysql.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import rx.Single

@Repository
class SysConfigDao @Autowired constructor(
    private val client: AsyncSQLClient
) : AbstractDao<User>(client) {
    override val log: Logger = logger(this::class)


    /**
     * 查询系统配置参数
     */
    fun querySysConfigList(): Single<List<JsonObject>> {
        val sql = SQL.init {
            SELECT("config_key as configKey", "config_value as configValue", "note")
            FROM(SysConfig.tableName)
            // 默认条件是 status = 0  && deleted_at = 0
            WHERES(listOf(Pair("status", 0), Pair("deleted_at", 0)))
        }
        return select(sql)

    }

    /**
     * 更新系统参数（单项）
     */
    fun editSysConfig(sysConfig: SysConfig): Single<UpdateResult> {

        val sql = "UPDATE ${SysConfig.tableName} " +
                "SET  config_value = '${sysConfig.configValue}', `updated_at` =${System.currentTimeMillis()} " +
                "WHERE `config_key` = '${sysConfig.configKey}' and `status` = 0  and `deleted_at` = 0 ;"

        log.info("editSysConfig() sql: $sql")
        return this.client.rxUpdate(sql)
    }


}
