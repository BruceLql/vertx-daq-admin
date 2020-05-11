package fof.daq.service.mongo.model


import fof.daq.service.common.extension.logger
import fof.daq.service.mongo.component.AbstractModel
import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.component.SortOrder
import fof.daq.service.mongo.schema.CarrierPyServer
import io.vertx.core.json.JsonObject
import io.vertx.rxjava.ext.mongo.MongoClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import rx.Single


@Repository
class CarrierPyServerModel @Autowired constructor(val client: MongoClient) :
    AbstractModel<CarrierPyServer>(client, CarrierPyServer.TABLE_NAME, CarrierPyServer::class.java) {

    override val log = logger(this::class)

    private val tableName = CarrierPyServer.TABLE_NAME

    fun listPageData(query: JsonObject, page: Int, size: Int): Single<PageItemModel<JsonObject>> {
        // 默认按 timestamp 排序
        val sortKey: String = "heart_beat_time"
        // 默认 降序
        val sortOrder: SortOrder = SortOrder.DESC

        return listPage(query,page,size,sortKey,sortOrder)
    }

    fun updateSwitch(objects: JsonObject): Single<JsonObject> {
//        update()
        val query = JsonObject().put("_id", objects.getValue("_id"))
        val document = JsonObject().put("switch", objects.getValue("switch"))
        val update = JsonObject().put("\$set", document)
        val startTime = System.currentTimeMillis()
        return this.client.rxUpdateCollection(tableName,query,update)
            .doAfterTerminate { logger(document, startTime) }
            .map { it.toJson() }
    }


}
