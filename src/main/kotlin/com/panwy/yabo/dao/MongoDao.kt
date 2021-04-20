package com.panwy.yabo.dao

import com.panwy.yabo.anno.Field
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component

@Component
class MongoDao(val mongoTemplate: MongoTemplate) {

    /**
     * 数据保存
     */
    fun save(data: MongoData): Unit {
        mongoTemplate.save(data)
    }

    fun list(clazz: Class<MongoData>): Unit {
        mongoTemplate.findAll(clazz)
    }

    fun delete(data: MongoData){
        mongoTemplate.remove(data)
    }

}

@Document
open class MongoData: Biz {
    @Id
    var id:String? = null
    private var bizKey:String? = null

    fun getBizKey():String?{
        return bizKey()
    }

    fun setBizKey( bizKey: String){
        this.bizKey = bizKey
    }

    fun genBizKey(){
        this.bizKey = bizKey()
    }

}

interface Biz {

    /**
     * 生成业务关键词
     */
    fun bizKey():String? {
        return null
    }

}

/**
 * 业务模型基础数据单元
 *
 * @param T
 * @property name
 * @property value
 * @constructor Create empty Bo unit
 */
open class BoUnit<T>: MongoData(){
    @Field("名称")
    var name:String = ""
    @Field("值")
    var value: T? = null

    override fun bizKey(): String {
        return name
    }
}