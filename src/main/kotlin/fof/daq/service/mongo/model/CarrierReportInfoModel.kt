package fof.daq.service.mongo.model


import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.rxjava.ext.mongo.MongoClient
import fof.daq.service.mongo.component.AbstractModel
import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.component.SortOrder
import fof.daq.service.mongo.schema.CarrierReportInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import rx.Single
import java.util.stream.Collectors


@Repository
class CarrierReportInfoModel @Autowired constructor(val client: MongoClient) :
    AbstractModel<CarrierReportInfo>(client, CarrierReportInfo.TABLE_NAME, CarrierReportInfo::class.java) {

    override val log = fof.daq.service.common.extension.logger(this::class)

    private val tableName = CarrierReportInfo.TABLE_NAME
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
     *  将运营商报告结果 or 运营商原始数据结果 插入Mongo库
     */
    fun insertReportDataIntoMongo(carrierReportInfo: CarrierReportInfo): Single<CarrierReportInfo> {
        carrierReportInfo.preInsert()
        return add(carrierReportInfo)
    }

    /**
     * 查询最近一次的采集结果的任务ID
     */
    fun queryLatestTaskId(query: JsonObject, findOptions: FindOptions): Single<List<JsonObject>> {

        val startTime = System.currentTimeMillis()
        return this.client.rxFindWithOptions(tableName, query, findOptions)
            .doAfterTerminate { logger(query, startTime) }
    }

    /**
     * 查询历史数据 列表
     * @param query 查询条件
     * @param page  当前页
     * @param size  返回条数
     */
    fun queryHistoryListResultData(query: JsonObject, page: Int, size: Int): Single<PageItemModel<JsonObject>> {
        // 默认按原始数据查
        query.put("item", "raw")
        val findOptions: FindOptions = FindOptions()
        findOptions.fields =
            JsonObject().put("mobile", 1).put("task_id", 1).put("name", 1).put("idCard", 1)
                .put("created_at", 1).put("_id", 0)
        return listPage(query,page,size,"created_at", SortOrder.DESC,findOptions)
    }

    /**
     * 查询具体的运营商数据（report:报告 or raw:原始数据）
     * raw :提供下载文件转成文件流传输到前端
     * report: 转成流传输到前端
     */
    fun queryDetialData(mobile: String, taskId: String, item: String): Single<List<JsonObject>> {
        val query = JsonObject()
        query.put("mobile", mobile).put("task_id", taskId).put("item", item)
        val findOption = FindOptions()
        findOption.setSort(JsonObject().put("created_at", -1)).limit = 1
        return queryListResultData(query,findOption)
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

}
