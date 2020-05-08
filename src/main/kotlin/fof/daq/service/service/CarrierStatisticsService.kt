package fof.daq.service.service

import fof.daq.service.mysql.component.PageItemDao
import fof.daq.service.mysql.dao.CarrierStatisticsDao
import fof.daq.service.mysql.entity.CarrierStatistics
import io.vertx.core.json.JsonObject
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rx.Single

/**
 * 运营商爬虫任务数据统计
 *
 */
@Service
class CarrierStatisticsService @Autowired constructor(
    private val client: AsyncSQLClient,
    private var carrierStatisticsDao: CarrierStatisticsDao
) {

    /**
     * 采集任务结束后 存储采集结果记录
     */
    fun dataInsertToMysql(carrierStatistics: CarrierStatistics): Single<CarrierStatistics>? {

        return this.client.rxGetConnection().flatMap { conn ->
            carrierStatisticsDao.insert(conn, carrierStatistics)
                .doAfterTerminate(conn::close)
        }
    }

    /**
     * 采集数据汇总查询  (不分页)
     */
    fun queryTotal(startTime: String, endTime: String): Single<MutableList<List<JsonObject>>> {
        println("$startTime +++++++++++++++++++++:$startTime")
        val queryTotal = carrierStatisticsDao.queryTotal(startTime, endTime)
        val groupByOperator = carrierStatisticsDao.queryGroupByOperator(startTime, endTime)
        return Single.concat(
            queryTotal,
            groupByOperator
        ).toList().toSingle()
    }

    /**
     *  按运营商类型、地区分组查询采集统计信息  分页查询
     */
    fun queryGroupByOperatorAndAreaListPage(
        startTime: String,
        endTime: String,
        currentPage: Int,
        size: Int
    ): Single<PageItemDao<JsonObject>> {

        return carrierStatisticsDao.queryGroupByOperatorAndArea(startTime, endTime, currentPage, size)

    }


}
