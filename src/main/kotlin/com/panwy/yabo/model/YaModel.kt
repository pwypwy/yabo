package com.panwy.yabo.model

import java.lang.reflect.Method


/**
 * 数据模型
 *
 * @property name 名称
 * @property fields 字段
 * @property methods 方法
 * @constructor Create empty Data model
 */
data class YaModel(
    val name: String,
    val className: String,
    val fields: List<YaField>,
    val funcs: List<YaMethod>,
    val services: List<BoService>,
    val clazz: Class<*>?
){
    /**
     *
     */
    fun toOut(): YaModelOut {
        return YaModelOut(name, className, fields.map { it.toOut() }, funcs.map { it.toOut() }, services.map { it.toOut() })
    }
}

data class YaModelOut(
    val name: String,
    val className: String,
    val fields: List<YaFieldOut>,
    val funcs: List<YaMethodOut>,
    val services: List<BoServiceOut>
)

/**
 * 数据字段
 *
 * @property fieldName 字段名称
 * @property name 业务名称
 * @property type 字段类型
 * @constructor Create empty Data field
 */
data class YaField(
    val fieldName: String,
    val name: String,
    val type: String,
    val clazzStr: String,
    val clazz: Class<*>?,
    val enumArray:List<String> = listOf(),
    val enumFunc:String = ""
){
    fun toOut(): YaFieldOut {
        return YaFieldOut(fieldName, name, type, clazzStr, enumArray, enumFunc)
    }
}

data class YaFieldOut(
    val fieldName: String,
    val name: String,
    val type: String,
    val clazzStr: String,
    val enumArray:List<String> = listOf(),
    val enumFunc:String = ""
)

/**
 * 方法
 *
 * @property funcName 函数名
 * @property name 业务名称
 * @property type 类型
 * @property parameter 参数
 * @constructor Create empty Ya method
 */
data class YaMethod(
    val funcName: String,
    val name: String,
    val type: String,
    val parameter: List<String>,
    val returnType: String,
    val method: Method,
    val from: String = ""
){
    /**
     *
     */
    fun toOut(): YaMethodOut {
        return YaMethodOut(funcName, name, type, parameter, returnType, from)
    }
}

data class YaMethodOut(
    val funcName: String,
    val name: String,
    val type: String,
    val parameter: List<String>,
    val returnType: String,
    val from: String = ""
)


data class YaboService(
    val name: String,
    val bean: Any,
    val funcs: Map<String, YaMethod>
){
    /**
     * 调用服务
     *
     * @param funcName
     * @param parameters
     * @return
     */
    fun call(funcName: String, vararg parameters: Any):Any?{
        return funcs[funcName]?.method?.invoke(bean, *parameters)
    }
}

data class BoService(
    val serviceName: String,
    val bizName: String,
    val bean: Any,
    val func: YaMethod
){
    /**
     * 调用服务
     *
     * @param funcName
     * @param parameters
     * @return
     */
    fun call(vararg parameters: Any):Any?{
        return func.method.invoke(bean, *parameters)

    }

    /**
     *
     */
    fun toOut(): BoServiceOut {
        return BoServiceOut(serviceName, bizName, func.toOut())
    }
}

data class BoServiceOut(
    val serviceName: String,
    val bizName: String,
    val func: YaMethodOut
)

/**
 * 总体数据
 *
 * @param T
 * @constructor Create empty All
 */
class All<T>{
    val list = mutableListOf<T>()
}

/**
 * 提示确认框
 *
 * @param T
 * @property data
 * @constructor Create empty Ya alert
 */
data class YaAlert<T>(val data:T,val msg:String = "")

data class YaFile(val name:String = "file",val data:Any,val type:String = "txt")


