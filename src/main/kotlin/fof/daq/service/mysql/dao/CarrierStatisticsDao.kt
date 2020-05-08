package fof.daq.service.mysql.dao

import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.sql.UpdateResult
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import fof.daq.service.common.extension.logger
import fof.daq.service.mysql.entity.CarrierStatistics
import fof.daq.service.mysql.component.AbstractDao
import fof.daq.service.mysql.component.PageItemDao
import fof.daq.service.mysql.component.SQL
import fof.daq.service.mysql.entity.SysConfig
import fof.daq.service.mysql.entity.User
import io.vertx.rxjava.ext.sql.SQLConnection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import rx.Single

@Repository
class CarrierStatisticsDao @Autowired constructor(
    private val client: AsyncSQLClient
) : AbstractDao<User>(client) {
    override val log: Logger = logger(this::class)


    /**
     * 插入爬虫任务执行情况
     * */
    fun insert(conn: SQLConnection, carrierStatistics: CarrierStatistics): Single<CarrierStatistics> {
        val sql = SQL.init {
            INSERT_INTO(carrierStatistics.tableName())
            carrierStatistics.preInsert().forEach { t, u -> VALUES(t, u) }
        }
        return this.update(conn, sql).map {
            carrierStatistics.apply { this.id = it.keys.getLong(0) }
        }
    }

    /**
     * 合计：总成功率、最低耗时、最高耗时、平均耗时
     */
    fun queryTotal(startTime: String, endTime: String): Single<List<JsonObject>> {

        val sql = "SELECT \n" +
                "\"ALL\" operator,"+
                "\tcount(*) count,\n" +
                "\tROUND(sum(IF(c.sucess is TRUE, 1,0)) /count(*)*100,2) sucRat,\n" +
                "\tMIN( c.statistics ) min,\n" +
                "\tMAX( c.statistics ) max,\n" +
                "\tcast(ROUND(avg( c.statistics ))as signed)  avg\n" +
                " FROM \n" +
                "\t`${CarrierStatistics.tableName}` c \n" +
                " WHERE  c.created_at/1000 BETWEEN unix_timestamp(\"${startTime}\") AND unix_timestamp(\"${endTime}\")"
        return this.select(sql)

    }


    /**
     * 按运营商分组：总成功率、最低耗时、最高耗时、平均耗时
     */
    fun queryGroupByOperator(startTime: String, endTime: String): Single<List<JsonObject>> {

        val sql = "SELECT \n" +
                "\tc.operator,\n" +
                "\tcount(*) count,\n" +
                "\tROUND(sum(IF(c.sucess is TRUE, 1,0)) /count(*)*100,2) sucRat,\n" +
                "\tMIN( c.statistics ) min,\n" +
                "\tMAX( c.statistics ) max,\n" +
                "\tcast(ROUND(avg( c.statistics ))as signed)  avg\n" +
                " FROM \n" +
                "\t`${CarrierStatistics.tableName}` c \n" +
                " WHERE  c.created_at/1000 BETWEEN unix_timestamp(\"${startTime}\") AND unix_timestamp(\"${endTime}\")\n" +
                "\n" +
                " GROUP BY\n" +
                "\tc.operator"
        return this.select(sql)
    }

    /**
     * 按运营商、地区分组：总成功率、最低耗时、最高耗时、平均耗时
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param currentPage 当前页
     * @param size 每页条数
     */
    fun queryGroupByOperatorAndArea(
        startTime: String,
        endTime: String,
        currentPage: Int,
        size: Int
    ): Single<PageItemDao<JsonObject>> {

        val sqlCount = "SELECT count(*) from\n" +
                "(\n" +
                "SELECT \n" +
                "\t*\n" +
                " FROM\n" +
                "\t`carrier_statistics` c \n" +
                " WHERE  c.created_at/1000 BETWEEN unix_timestamp(\"${startTime}\") AND unix_timestamp(\"${endTime}\")\n"+
                " GROUP BY\n" +
                "\tc.operator,c.province,c.city\n" +
                "\t) ss"

        val sqlList = "SELECT \n" +
                "\tc.operator,\n" +
                "\tc.province,\n" +
                "\tc.city,\n" +
                "\tcount(*) count,\n" +
                "\tROUND(sum(IF(c.sucess is TRUE, 1,0)) /count(*)*100,2) sucRat,\n" +
                "\tMIN( c.statistics ) min,\n" +
                "\tMAX( c.statistics ) max,\n" +
                "\tcast(ROUND(avg( c.statistics ))as signed)  avg\n" +
                " FROM\n" +
                "\t`${CarrierStatistics.tableName}` c \n" +
                " WHERE  c.created_at/1000 BETWEEN unix_timestamp(\"${startTime}\") AND unix_timestamp(\"${endTime}\")\n" +
                " GROUP BY\n" +
                "\tc.operator,c.province,c.city\n" +
                " ORDER BY c.province,c.city,count DESC"

        println("------ sqlCount: $sqlCount")
        println("------ sqlList: $sqlList")
        return this.listPage(sqlCount,sqlList,currentPage ,size)
    }


}
