package com.panwy.yabo.anno

import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target(AnnotationTarget.CLASS)
annotation class Bo(
    val name: String
)

@Target(AnnotationTarget.FIELD)
annotation class Field(
    val name: String,
    val type: String = ""
)

@Target(AnnotationTarget.FUNCTION)
annotation class Func(
    val name: String,
    val allow4field: String = "",
    val allow4js: String = "",
    val allow4kts: String = ""
)

@Target( AnnotationTarget.VALUE_PARAMETER)
annotation class From

@Target( AnnotationTarget.CLASS)
@Service
annotation class YaService

@Target( AnnotationTarget.VALUE_PARAMETER)
annotation class Enum(
    val array:Array<String> = [],
    val func:String = ""
)

@Target(AnnotationTarget.FUNCTION)
annotation class Attach(
    val name: String,
    val visible: Boolean = false
)

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
annotation class BizKey(
    val name: String
)

//
@Target(AnnotationTarget.FIELD)
annotation class Ref(
    val name: String
)

/**
 * 元组
 *
 * @property name
 * @constructor Create empty Tuple
 */
@Target(AnnotationTarget.CLASS)
annotation class Tuple(
    val name: String
)


@Target(AnnotationTarget.CLASS)
annotation class Show(
    val name: String
)

@Target(AnnotationTarget.FUNCTION)
annotation class YaShow(
    val name: String
)


enum class FieldType(name:String){
    TEXT("text"),CODE("code")
}