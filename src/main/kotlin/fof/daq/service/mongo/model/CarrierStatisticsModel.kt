package fof.daq.service.mongo.model


import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.rxjava.ext.mongo.MongoClient
import fof.daq.service.mongo.component.AbstractModel
import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.component.SortOrder
import fof.daq.service.mongo.schema.CarrierStatistics
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import rx.Observable
import rx.Single
import java.util.stream.Collectors


@Repository
class CarrierStatisticsModel @Autowired constructor(val client: MongoClient) :
    AbstractModel<CarrierStatistics>(client, CarrierStatistics.TABLE_NAME, CarrierStatistics::class.java) {

    override val log = fof.daq.service.common.extension.logger(this::class)

    private val tableName = CarrierStatistics.TABLE_NAME
    /**
     * 查询数据结果
     */
    fun queryListResultData(query: JsonObject, findOptions: FindOptions): Single<List<JsonObject>> {
        println(query.toString() + "-----------------------" + findOptions.toJson())
        val startTime = System.currentTimeMillis()
        return this.client.rxFindWithOptions(tableName, query, findOptions)
            .doAfterTerminate { logger(query, startTime) }
    }



    /**
     * 翻页查询
     * @param query 查询条件
     * @param page
     * @param size 返回数量
     * @param sortKey 排序键
     * @param sortOrder 排序方式
     */
     fun listPage(
        query: JsonObject,
        page: Int,
        size: Int,
        sortKey: String,
        sortOrder: SortOrder,
        options: FindOptions
    ): Single<PageItemModel<JsonObject>> {
        options.setLimit(size)
            .setSkip((page - 1) * size)
            .setSort(JsonObject().put(sortKey, sortOrder.value))

        val countObservable = client.rxCount(collectionName, query)
        val documentsObservable = client.rxFindWithOptions(collectionName, query, options)
        val startTime = System.currentTimeMillis()
        return Single.zip(countObservable, documentsObservable) {
                count, documents ->
            val items = documents.stream().collect(Collectors.toList())
            PageItemModel(count, size, items)
        }.doAfterTerminate { logger(query, startTime) }
    }

    /**
     * 查询运营商汇总数据
     *  @param query 查询条件（时间区间）
     *  @param findOptions
     */
    fun queryTotalData(jsonArray:JsonArray): Observable<JsonObject>? {
        val startTime = System.currentTimeMillis()
        println("jsonArray mongo 查询条件：$jsonArray")

        return this.client.aggregate(tableName, jsonArray).toObservable()
    }

}
