package fof.daq.service.mongo.model


import fof.daq.service.common.extension.logger
import fof.daq.service.mongo.component.AbstractModel
import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.component.SortOrder
import fof.daq.service.mongo.schema.CarrierLoggingEvent
import io.vertx.core.json.JsonObject
import io.vertx.rxjava.ext.mongo.MongoClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import rx.Single


@Repository
class CarrierLoggingEventModel @Autowired constructor(val client: MongoClient) :
    AbstractModel<CarrierLoggingEvent>(client, CarrierLoggingEvent.TABLE_NAME, CarrierLoggingEvent::class.java) {

    override val log = logger(this::class)

    private val tableName = CarrierLoggingEvent.TABLE_NAME

    fun listPageData(query: JsonObject, page: Int, size: Int): Single<PageItemModel<JsonObject>> {
        // 默认按 record_time 排序
        val sortKey: String = "record_time"
        // 默认 降序
        val sortOrder: SortOrder = SortOrder.DESC

        return listPage(query,page,size,sortKey,sortOrder)
    }

}
