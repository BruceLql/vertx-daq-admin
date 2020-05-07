package fof.daq.service

import fof.daq.service.service.CarrierStatisticsService
import fof.daq.service.web.WebVerticle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import tech.kavi.vs.core.LauncherVerticle
import tech.kavi.vs.web.HandlerRequestAnnotationBeanName

@Import(BeanConfig::class)
@ComponentScan(nameGenerator = HandlerRequestAnnotationBeanName::class)
class MainVerticle : LauncherVerticle() {


    @Autowired
    private lateinit var webVerticle: WebVerticle
    @Autowired
    private lateinit var carrierStatisticsService: CarrierStatisticsService

    @Throws(Exception::class)
    override fun start() {
        super.start()
        vertx.deployVerticle(webVerticle)

//        carrierStatisticsService.dataSelect("2020-05-05 00:00:00", "2020-05-08 00:00:00")?.subscribe({
//            println("??????????????????????????????????")
//            println( "=====================:$it" )
//        },{it.printStackTrace()})

    }

    companion object {
        @JvmStatic
        fun main(args:Array<String>) {
            // 初始化类
            launcher(fof.daq.service.MainVerticle::class.java)
        }
    }
}
