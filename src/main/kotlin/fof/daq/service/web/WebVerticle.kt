package fof.daq.service.web

import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.value
import fof.daq.service.web.admin.AdminController
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*

/**
 * Web模块
 * */
@Component
class WebVerticle : AbstractVerticle() {

    private val log = logger(this::class)

    @Autowired
    @Qualifier("config")
    private lateinit var config: JsonObject

    /**
     * 注入全局路由
     * */
    @Autowired
    private lateinit var router: Router

    /**
     * Web路由全局处理器
     * */
    @Autowired
    private lateinit var webHandler: WebRouterHandler

    /**
     * 后台管理控制器
     * */
    @Autowired
    private lateinit var adminController: AdminController


    @Throws(Exception::class)
    override fun start() {

        /**
         * 许可跨域授权 (注：直接设置全局*是无效的，必须试用绝对路径地址)
         * */
        val allowedHeaders = HashSet<String>()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("X-PINGARUNER")
        val allowedMethods = HashSet<HttpMethod>()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)
        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PATCH)
        allowedMethods.add(HttpMethod.PUT)
        /*全局路由监听*/
        config.value<String>("CORS")?.also {
            // 测试时路径可以写成  .*
            router.route().
                handler(CorsHandler.create(it).allowedHeaders(allowedHeaders).allowedMethods(allowedMethods).allowCredentials(true))
            log.info("Add Cors address: $it")
        }


        /*全局路由监听*/
        router.route().handler(webHandler::routerHandler)

        /*管理后台入口*/
        adminController.create(router, "/admin")

        /*全局路由错误处理*/
        router.route().failureHandler(webHandler::failureHandler)

        /**
         * 加载静态目录文件, 兼容SPA模式，找不到文件返回至跟路径
         * [ 注意GET为前端请求，POST为后台数据访问 ]
         * */
        router.route("/static/*").handler(StaticHandler.create())


        /*HTTP端口监听*/
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config.value("HTTP.PORT", 80)){
            if (it.succeeded()) {
                log.info("成功启动监听端口:${it.result().actualPort()}")
            } else {
                log.error(it.cause())
            }
        }
    }
}
