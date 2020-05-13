package fof.daq.service.common.auth

import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.principal
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.handler.impl.AuthHandlerImpl
import io.vertx.ext.web.handler.impl.HttpStatusException
import fof.daq.service.common.extension.success
import fof.daq.service.common.extension.value
import fof.daq.service.mysql.entity.User
import org.springframework.stereotype.Service

@Service
class MybatisAuthHandler(authProvider: AuthProvider) : AuthHandlerImpl(authProvider) {
    private val log = logger(this::class)
    /**
     * 数据访问验证
     * */
    override fun parseCredentials(context: RoutingContext, handler: Handler<AsyncResult<JsonObject>>) {
        println("============================:数据访问验证")
        val session = context.session()
        if (session != null) {

            println("==========================有session : ${session.toString()}")

            handler.handle(Future.failedFuture<JsonObject>(HttpStatusException(401)))
        } else {
            println("==========================:无session")
            handler.handle(Future.failedFuture("No session - did you forget to include a SessionHandler?"))
        }
    }

    /**
     * 表单登录验证
     * */
    fun login(context: RoutingContext) {
        log.info("----- 开始登陆：login() -------")
        val bodyAsJson = context.bodyAsJson
        val req = context.request()
        if (req.method() != HttpMethod.POST) {
            context.fail(405)
            return
        }

        val params = req.formAttributes()
        val username = bodyAsJson.value<String>("username")
        val password = bodyAsJson.value<String>("password")
        log.info("----- 登陆用户是：[$username] -------")
        /**
         * 校验用户名与密码是否存在
         * */
        if (username.isNullOrEmpty()|| password.isNullOrEmpty()) {
            context.fail(400, NullPointerException("No username or password provided in form?"))
            return
        }
        val session = context.session()
        val authInfo = JsonObject().put("username", username).put("password", password)
        authProvider.authenticate(authInfo) { ar ->
            if (ar.succeeded()) {
                val user = ar.result()
                context.setUser(user)
                session?.regenerateId()
                log.info("用户：[$username] 登录成功！")
                req.response().success(user.principal())
            } else {
                log.info("用户：[$username] 登录失败！")
                /*返回错误状态*/
                context.fail(ar.cause())
            }
        }
    }

    /**
     * 退出登录状态
     */
    fun exit(context: RoutingContext) {
        log.info("---- 退出登录！-----")
        context.clearUser()
        context.response().success(true)
    }

}
