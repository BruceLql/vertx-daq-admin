package fof.daq.service.service

import com.mongodb.BasicDBObject
import fof.daq.service.common.extension.value
import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.model.CarrierStatisticsModel
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rx.Observable
import rx.Single
import java.math.BigDecimal
import kotlin.math.roundToInt

/**
 * 运营商爬虫任务数据统计
 *
 */
@Service
class CarrierStatisticsService @Autowired constructor(
    private var carrierStatisticsModel: CarrierStatisticsModel
) {

    /**
     * 采集任务结束后 存储采集结果记录
     */
/*    fun dataInsertToMysql(carrierStatistics: CarrierStatistics): Single<CarrierStatistics>? {

        return this.client.rxGetConnection().flatMap { conn ->
            carrierStatisticsDao.insert(conn, carrierStatistics)
                .doAfterTerminate(conn::close)
        }
    }*/

    /**
     * 采集数据汇总查询  (不分页)
     */
    fun queryTotal(startTime: String, endTime: String): Single<JsonArray> {
        println("$startTime +++++++++++++++++++++:$startTime")
        /*val queryTotal = carrierStatisticsDao.queryTotal(startTime, endTime)
        val groupByOperator = carrierStatisticsDao.queryGroupByOperator(startTime, endTime)
        return Single.concat(
            queryTotal,
            groupByOperator
        ).toList().toSingle()*/
        return queryTotalNoListPage(startTime, endTime)
    }

    /**
     *  按运营商类型、地区分组查询采集统计信息  分页查询
     */
    fun queryGroupByOperatorAndAreaListPage(
        startTime: String,
        endTime: String,
        currentPage: Int,
        size: Int
    ): Single<PageItemModel<JsonObject>> {

//        return carrierStatisticsDao.queryGroupByOperatorAndArea(startTime, endTime, currentPage, size)
        return queryGroupByOperatorAndCityListPage(startTime, endTime, currentPage, size)

    }

    /**
     *  mongo 运营商汇总查询
     *  @param dateStart 时间格式字符串
     *  @param dateEnd  时间格式字符串
     */
    fun queryGroupByAll(dateStart: String, dateEnd: String): Observable<MutableList<JsonObject>> {
        val query = JsonObject()

        query.put(
            "created_at",
            BasicDBObject("\$gte", JsonObject().put("\$date", dateStart.replace(" ", "T") + "Z")).append(
                "\$lte",
                JsonObject().put("\$date", dateEnd.replace(" ", "T") + "Z")
            )
        )
        val all = BasicDBObject("_id", null)
            .append("avg", BasicDBObject("\$avg", "\$statistics"))
            .append("max", BasicDBObject("\$max", "\$statistics"))
            .append("min", BasicDBObject("\$min", "\$statistics"))
            .append("count", BasicDBObject("\$sum", 1))
            .append(
                "sucCount", BasicDBObject(
                    "\$sum",
                    BasicDBObject(
                        "\$cond",
                        JsonArray().add(
                            BasicDBObject(
                                "\$ne",
                                JsonArray().add("\$sucess").add(true)
                            )
                        ).add(0).add(1)
                    )
                )
            )
        val match = BasicDBObject("\$match", query)
        val group = BasicDBObject("\$group", all)
        val jsonArray = JsonArray()
        jsonArray.add(match)
        jsonArray.add(group)


        return carrierStatisticsModel.queryTotalData(jsonArray)
    }

    /**
     *  mongo 按运营商分组汇总查询
     *  @param dateStart 时间格式字符串
     *  @param dateEnd  时间格式字符串
     */
    fun queryGroupByOpeertor(dateStart: String, dateEnd: String): Observable<MutableList<JsonObject>> {
        val query = JsonObject()

        query.put(
            "created_at",
            BasicDBObject("\$gte", JsonObject().put("\$date", dateStart.replace(" ", "T") + "Z")).append(
                "\$lte",
                JsonObject().put("\$date", dateEnd.replace(" ", "T") + "Z")
            )
        )
        val all = BasicDBObject("_id", BasicDBObject("operator", "\$operator"))
            .append("avg", BasicDBObject("\$avg", "\$statistics"))
            .append("max", BasicDBObject("\$max", "\$statistics"))
            .append("min", BasicDBObject("\$min", "\$statistics"))
            .append("count", BasicDBObject("\$sum", 1))
            .append(
                "sucCount", BasicDBObject(
                    "\$sum",
                    BasicDBObject(
                        "\$cond",
                        JsonArray().add(
                            BasicDBObject(
                                "\$ne",
                                JsonArray().add("\$sucess").add(true)
                            )
                        ).add(0).add(1)
                    )
                )
            )
        val match = BasicDBObject("\$match", query)
        val group = BasicDBObject("\$group", all)
        val sort = BasicDBObject("\$sort", JsonObject().put("count", -1).put("_id.operator", -1))
        val jsonArray = JsonArray()
        jsonArray.add(match)
        jsonArray.add(group)
        jsonArray.add(sort)


        return carrierStatisticsModel.queryTotalData(jsonArray)
    }

    /**
     *  mongo 按运营商类型、城市分组汇总查询
     *  @param dateStart 时间格式字符串
     *  @param dateEnd  时间格式字符串
     */
    fun queryGroupByOpeertorAndCity(dateStart: String, dateEnd: String): Observable<MutableList<JsonObject>> {
        val query = JsonObject()

        query.put(
            "created_at",
            BasicDBObject("\$gte", JsonObject().put("\$date", dateStart.replace(" ", "T") + "Z")).append(
                "\$lte",
                JsonObject().put("\$date", dateEnd.replace(" ", "T") + "Z")
            )
        )
        val all = BasicDBObject("_id", JsonObject().put("operator", "\$operator").put("city", "\$city"))
            .append("avg", BasicDBObject("\$avg", "\$statistics"))
            .append("max", BasicDBObject("\$max", "\$statistics"))
            .append("min", BasicDBObject("\$min", "\$statistics"))
            .append("count", BasicDBObject("\$sum", 1))
            .append(
                "sucCount", BasicDBObject(
                    "\$sum",
                    BasicDBObject(
                        "\$cond",
                        JsonArray().add(
                            BasicDBObject(
                                "\$ne",
                                JsonArray().add("\$sucess").add(true)
                            )
                        ).add(0).add(1)
                    )
                )
            )
        val match = BasicDBObject("\$match", query)
        val group = BasicDBObject("\$group", all)
        val sort = BasicDBObject("\$sort", JsonObject().put("_id.city", -1).put("_id.operator", -1))
        val jsonArray = JsonArray()
        jsonArray.add(match)
        jsonArray.add(group)
        jsonArray.add(sort)

        return carrierStatisticsModel.queryTotalData(jsonArray)
    }


    /**
     *  查询汇总数据 不分页
     */
    fun queryTotalNoListPage(dateStart: String, dateEnd: String): Single<JsonArray> {
        val totalObserv = queryGroupByAll(dateStart, dateEnd)
        val byOperatorObserv = queryGroupByOpeertor(dateStart, dateEnd)

        return Single.zip(totalObserv.toSingle(), byOperatorObserv.toSingle()) { total, byOperator ->
            val _total = total.map {
                if (it.value<String>("operator").isNullOrEmpty()
                    && it.value<JsonObject>("_id")?.value<String>("operator").isNullOrEmpty()
                ) {
                    it.put("operator", "ALL")
                    it.remove("_id")
                }
                var sucRat = 0.00
                val countValue = it.value<Int>("count") ?: 0
                val sucCount = it.value<Int>("sucCount") ?: 0

                it.put("avg", (it.getValue("avg") as Double).roundToInt())
                if (countValue > 0) sucRat =
                    BigDecimal(sucCount * 100).divide(BigDecimal(countValue), 2, BigDecimal.ROUND_HALF_UP).toDouble()
                it.put("sucRat", sucRat)
            }
            val returnDate = JsonArray().add(_total[0])

            byOperator.mapNotNull {
                if (!it.value<JsonObject>("_id")?.value<String>("operator").isNullOrEmpty()) {
                    it.value<JsonObject>("_id")?.mapNotNull { id_ ->
                        it.put(id_.key, id_.value)
                    }
                    it.remove("_id")
                }
                var sucRat = 0.00
                val countValue = it.value<Int>("count") ?: 0
                val sucCount = it.value<Int>("sucCount") ?: 0

                it.put("avg", (it.getValue("avg") as Double).roundToInt())
                if (countValue > 0) sucRat =
                    BigDecimal(sucCount * 100).divide(BigDecimal(countValue), 2, BigDecimal.ROUND_HALF_UP).toDouble()
                it.put("sucRat", sucRat)
                returnDate.add(it)
            }

            returnDate
        }

    }

    /**
     *  查询按运营商类型、城市分组数据 分页
     */
    fun queryGroupByOperatorAndCityListPage(
        dateStart: String,
        dateEnd: String,
        page: Int,
        size: Int
    ): Single<PageItemModel<JsonObject>> {

        return carrierStatisticsModel.aggregateListPage(dateStart, dateEnd, page, size)

    }

}
