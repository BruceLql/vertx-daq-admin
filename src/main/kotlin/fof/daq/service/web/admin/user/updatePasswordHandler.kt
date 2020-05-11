package fof.daq.service.web.admin.user

import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.success
import fof.daq.service.common.extension.value
import fof.daq.service.mysql.dao.UserDao
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.DecodeException
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler
import io.vertx.ext.web.handler.impl.HttpStatusException
import org.springframework.beans.factory.annotation.Autowired
import tech.kavi.vs.web.ControllerHandler
import tech.kavi.vs.web.HandlerRequest

@HandlerRequest(path = "/updatePassword", method = HttpMethod.POST)
class updatePasswordHandler @Autowired constructor(
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
        log.info("===========/user/updatePassword ===================")
        try {
            val obj = try {
                event.bodyAsJson
            } catch (e: DecodeException) {
                event.fail(HttpStatusException(400, e))
                return
            }
            log.info("传入参数：$obj")
            val username = obj.value<String>("username")
            val oldPassword = obj.value<String>("oldPassword")
            val password = obj.value<String>("password")
            val password2 = obj.value<String>("password2")
            oldPassword ?: throw IllegalArgumentException("oldPassword 不合法！")
            username ?: throw IllegalArgumentException("username 不合法！")
            password ?: throw IllegalArgumentException("password 不合法！")
            if(!password.equals(password2) ) throw IllegalArgumentException("两次输入新密码不一致！")


            var result = false
            userDao.updatePassword(username, oldPassword, password).subscribe({
                if (it.id != null) result = true
                event.response().success(result)
            }, {
                it.printStackTrace()
                event.fail(it)
            })

        } catch (e: Exception) {
            e.printStackTrace()
            event.fail(e)
        }
    }
}
