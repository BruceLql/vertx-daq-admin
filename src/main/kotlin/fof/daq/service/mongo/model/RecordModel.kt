package fof.daq.service.mongo.model

import io.vertx.rxjava.ext.mongo.MongoClient
import fof.daq.service.common.extension.logger
import fof.daq.service.mongo.component.AbstractModel
import fof.daq.service.mongo.schema.Record
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository


@Repository
class RecordModel @Autowired constructor(val client: MongoClient) : AbstractModel<Record>(client, Record.tableName, Record::class.java){

    override val log = logger(this::class)

    private val tableName = Record.tableName

}
