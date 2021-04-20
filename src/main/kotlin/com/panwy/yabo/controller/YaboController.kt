package com.panwy.yabo.controller

import com.alibaba.fastjson.JSON

import com.panwy.yabo.YaboCore
import com.panwy.yabo.dao.MongoData
import com.panwy.yabo.model.YaAlert
import com.panwy.yabo.model.YaFile
import com.mongodb.client.model.Filters.eq
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("yabo")
class YaboController(
    private val yaboCore: YaboCore,
    private val mongoTemplate: MongoTemplate
) {



    @GetMapping("what")
    fun test(): Any {
        println(333)
        return yaboCore.bizMap
    }

    /**
     * 获取模型集合
     */
    @PostMapping("yms")
    fun getYaModels(): YaResult<*> {
        return YaResult(data=yaboCore.bizMap)
    }

    /**
     *
     */
    @PostMapping("ym")
    fun getYaModel(@RequestBody param: YaParam): YaResult<*> {
        val ym = yaboCore.map[param.ym]?:yaboCore.bizMap[param.ym]?:return YaResult(404,"未定义业务对现象",null)

        val need = (yaboCore.map.map { it.value.toOut() }.associateBy { it.className })
        return YaResult(data= mapOf(
            "main" to ym.toOut(),
            "need" to need
        ))
    }

    /**
     * 
     */
    @PostMapping("api/self")
    fun selfOp(@RequestBody param: YaParam): YaResult<*> {
        try {
            val ym = yaboCore.map[param.ym]?:return YaResult(500,"未知业务实体: ${param.ym}",null)
            val bo = JSON.parseObject(JSON.toJSONString(param.param), ym.clazz)
            val method = ym.funcs.first { it.funcName == param.op }.method
            method.invoke(bo)
            mongoTemplate.save(bo)

            return  YaResult(data = null)
        }catch (e:Exception){
            return  YaResult(500,"接口异常: $e",e.localizedMessage)
        }
    }

    @PostMapping("api/service")
    fun serviceOp(@RequestBody param: YaParam): YaResult<*> {
        try {
            val ym = param.ym
            val ymClass = (yaboCore.map[ym]?:return YaResult(500,"未知业务实体: ${param.ym}",null)).clazz
            val op = param.op
            val fromData = JSON.parseObject(JSON.toJSONString(param.param), ymClass)
            val hashCode = fromData.hashCode()
            val result = yaboCore.boServiceTable[ym,op].call(fromData)
            if(hashCode != fromData.hashCode()){
                mongoTemplate.save(fromData)
            }

            return when(result){
                is YaAlert<*> -> YaResult(data = result,msg = "alert")
                is YaFile -> YaResult(data = result,msg = "file")
                else -> YaResult(data = result,msg = "ok")
            }

        }catch (e:Exception){
            e.printStackTrace()
            return  YaResult(500, "alert","接口异常: $e")
        }
    }


    @PostMapping("api/base")
    fun baseOp(@RequestBody param: YaParam): YaResult<*> {

        //dealByBson(param)
        val data: Any? = dealByJpa(param)

        return YaResult(data = data)
    }

    private fun dealByBson(param: YaParam): Any? {
        return when (param.op) {
            "list" -> {
                val list = mutableListOf<Any>()
                mongoTemplate.getCollection(param.ym).find().limit(100).forEach {
                    list.add(it)
                    it["_id"] = it["_id"].toString()
                }
                list
            }
            "save" -> {
                val data = param.param.toMutableMap()
                data["_id"]?.let {
                    data["_id"] = ObjectId(it.toString())
                }
                mongoTemplate.save<Any?>(data, param.ym)
            }
            "insert" -> {
                mongoTemplate.getCollection(param.ym).insertOne(Document(param.param))
            }
            "update" -> {
                mongoTemplate.getCollection(param.ym).updateOne(eq("_id", ""), eq("pass", "v"))
            }
            "delete" -> {
                val data = param.param.toMutableMap()
                data["_id"]?.let {
                    data["_id"] = ObjectId(it.toString())
                }
                mongoTemplate.remove(data, param.ym)
                //mongoTemplate.getCollection("").deleteOne(eq("_id",data["_id"]))
            }
            else -> null
        }
    }

    /**
     * 采用 spring data jpa 的方式完成数据持久化操作
     *
     * @param param
     * @return
     */
    private fun dealByJpa(param: YaParam): Any? {
        val ym = yaboCore.map[param.ym]?:return null
        val clazz = ym.clazz?:return null
        return when (param.op) {
            "list" -> {
                mongoTemplate.findAll(clazz)
            }
            "save" -> {
                val data = JSON.parseObject(JSON.toJSONString(param.param), ym.clazz)
                (data as MongoData).genBizKey()
                mongoTemplate.save(data)

            }
            "insert" -> {
                val data = JSON.parseObject(JSON.toJSONString(param.param), ym.clazz)
                mongoTemplate.insert(data)
            }
            "update" -> {
                //mongoTemplate.update()
            }
            "delete" -> {
                val data = JSON.parseObject(JSON.toJSONString(param.param), ym.clazz)
                mongoTemplate.remove(data)
                //mongoTemplate.getCollection("").deleteOne(eq("_id",data["_id"]))
            }
            else -> null
        }
    }


}

@org.springframework.data.mongodb.core.mapping.Document
data class M1(
    @Id
    val id:String?,
    var name:String
)

@org.springframework.data.mongodb.core.mapping.Document
data class M2(
    @Id
    val id:String?,
    val name:String,
    @DBRef
    val m1: M1?
)

data class YaParam(
    val ym: String,
    val op: String,
    val param: Map<String, Any>
)

data class YaResult<T>(
    val code: Int = 200,
    val msg: String = "ok",
    val data: T? = null
)

class YaJson{
    val map :MutableMap<String, YaJson> = mutableMapOf()

    operator fun get(key: String): YaJson {
        return map[key]?: YaJson()
    }

    operator fun set(key: String, value: YaJson){
        map[key] = value
    }
}

