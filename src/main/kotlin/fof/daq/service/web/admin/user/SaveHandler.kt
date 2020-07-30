package fof.daq.service.web.admin.user

import io.vertx.core.http.HttpMethod
import io.vertx.core.json.DecodeException
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler
import io.vertx.ext.web.handler.impl.HttpStatusException
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.success
import fof.daq.service.common.extension.toEntity
import fof.daq.service.common.extension.value
import fof.daq.service.mysql.dao.UserDao
import fof.daq.service.mysql.entity.User
import io.vertx.core.json.JsonObject

@HandlerRequest(path = "/save", method = HttpMethod.POST)
class SaveHandler @Autowired constructor(
    private val userDao: UserDao
) : ControllerHandler() {
    private val log = logger(this::class)

    /**
     * 参数校验过滤
     * */
    override fun route(route: Route) {
        route.handler(
            HTTPRequestValidationHandler.create()
//                .addFormParam("username", ParameterType.GENERIC_STRING, true)
//                .addFormParam("realname", ParameterType.GENERIC_STRING, true)
        )
    }

    /**
     * 数据处理
     * */
    override fun handle(event: RoutingContext) {
        try {

            val user = try {
                event.bodyAsJson.toEntity<User>()
            } catch (e: DecodeException) {
                event.fail(HttpStatusException(400, e))
                return
            }
            userDao.save(user)
                .map { it.toJson() } // bean转JSON会自动忽略密码等信息
                .subscribe({
                    if(!it.isEmpty && !it.value<String>("username").isNullOrEmpty())
                        event.response().success(true)
                    else
                        event.response().success(false)

                }, event::fail)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun test(){
        println("Just test demo")
    }
}
