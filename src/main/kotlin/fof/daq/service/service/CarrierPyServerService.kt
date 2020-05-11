package fof.daq.service.service

import fof.daq.service.mongo.component.PageItemModel
import fof.daq.service.mongo.model.CarrierPyServerModel
import io.vertx.core.json.JsonObject
import io.vertx.rxjava.ext.asyncsql.AsyncSQLClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import rx.Single

/**
 * 服务器列表
 *
 */
@Service
class CarrierPyServerService @Autowired constructor(
    private val client: AsyncSQLClient,
    private var carrierPyServerModel: CarrierPyServerModel
) {

    /**
     * 服务器列表查询 分页
     */
    fun pyServerList(query: JsonObject, page:Int, size: Int): Single<PageItemModel<JsonObject>> {

        return carrierPyServerModel.listPageData(query,page,size)

    }

    /**
     * 后台操作 开通/关闭 服务
     */
    fun updateSwitchById(objects :JsonObject): Single<JsonObject> {
        objects.getValue("_id")
        objects.getValue("switch")
       return carrierPyServerModel.updateSwitch(objects)

    }


}
