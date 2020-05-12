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
        log.info("-------------------login -----------")
        val bodyAsJson = context.bodyAsJson
        val req = context.request()
        if (req.method() != HttpMethod.POST) {
            context.fail(405)
            return
        }

        val params = req.formAttributes()
        val username = bodyAsJson.value<String>("username")
        val password = bodyAsJson.value<String>("password")

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
                println(ar.result().principal())
                val user = ar.result()
                println("==============user: ${user.principal<User>()}")
                context.setUser(user)
                session?.regenerateId()
                req.response().success(user.principal())
            } else {
                /*返回错误状态*/
                context.fail(ar.cause())
            }
        }
    }

    /**
     * 退出登录状态
     */
    fun exit(context: RoutingContext) {

        context.clearUser()
        context.response().success(true)
    }

}
