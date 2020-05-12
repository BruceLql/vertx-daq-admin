package fof.daq.service.web

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import fof.daq.service.common.extension.logger
import fof.daq.service.common.extension.value
import fof.daq.service.web.admin.AdminController
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.handler.CorsHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.HashSet

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

        /*********************配置跨域*****************************/
        val allowedHeaders: MutableSet<String> = HashSet()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("Access-Control-Allow-Headers")
        allowedHeaders.add("Access-Control-Allow-Method")
        allowedHeaders.add("Access-Control-Allow-Credentials")
        allowedHeaders.add("Authorization")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("X-PINGARUNER")
        val allowedMethods: MutableSet<HttpMethod> = HashSet()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)
        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PATCH)
        allowedMethods.add(HttpMethod.PUT)
        router.route().
            handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods))


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
//        router.route("/static/*").handler(StaticHandler.create())


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
