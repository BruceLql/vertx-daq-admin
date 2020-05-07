package fof.daq.service.service

import com.mongodb.BasicDBObject
import fof.daq.service.mongo.model.CarrierStatisticsModel
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rx.Observable
import java.text.SimpleDateFormat
import java.util.*

/**
 * 运营商爬虫任务数据统计
 *
 */
@Service
class CarrierStatisticsService @Autowired constructor(
    private var carrierStatisticsModel: CarrierStatisticsModel
) {

    fun dataSelect(startTime: String, endTime: String): Observable<JsonObject>? {
        val query = JsonObject()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.calendar = GregorianCalendar(SimpleTimeZone(0, "GMT"))

        query.put("created_at", BasicDBObject("\$gte", startTime).append("\$lte", startTime))
        val all = BasicDBObject("_id", "\$sucess")
            .append("ave_time", BasicDBObject("\$avg", "\$statistics"))
            .append("max_time", BasicDBObject("\$max", "\$statistics"))
            .append("min_time", BasicDBObject("\$min", "\$statistics"))
            .append("count", BasicDBObject("\$sum", 1))

        val match = BasicDBObject("\$match", query)
        val group = BasicDBObject("\$group", all)
        val jsonArray = JsonArray()
        jsonArray.add(match)
        jsonArray.add(group)

        println("OOOOOOOOOOOOOOOOOOOOOOOOO")
        return carrierStatisticsModel.queryTotalData(jsonArray)
    }


}
