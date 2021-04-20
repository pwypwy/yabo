package com.panwy.yabo


import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.panwy.yabo.anno.*
import com.panwy.yabo.model.*
import org.reflections.Reflections
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType
import javax.annotation.PostConstruct

@Component
class YaboCore(private val context:ApplicationContext) {

    @Value("\${yabo.scanPackage:}")
    val packageName:String = ""

    val map:MutableMap<String, YaModel> = mutableMapOf()

    val bizMap:MutableMap<String, YaModel> = mutableMapOf()

    val yaServiceMap:MutableMap<String, YaboService> = mutableMapOf()

    val boServiceTable: Table<String, String, BoService> = HashBasedTable.create()
    /**
     * Yabo框架初始化注册业务模型
     */
    @PostConstruct
    fun init() {

        val yaServices = context.getBeansWithAnnotation(YaService::class.java)

        yaServices.forEach { (k,v) ->

            val funcs = getFuncs(v::class.java,null, FuncType.OUT).associateBy { it.funcName }
            funcs.forEach{ (_, f) ->
                boServiceTable.put(f.from,f.funcName, BoService(f.funcName,f.name,v,f))
            }

            yaServiceMap[k] = YaboService(k,v,funcs)
        }

        val f = Reflections(packageName);
        // 获取扫描到的标记注解的集合
        val set = f.getTypesAnnotatedWith(Bo::class.java)
        //println("$packageName  setSize: " + set.size)
        set.forEach {

            val sc = it.superclass

            val name = it.getAnnotation(Bo::class.java).name

            val className = it.name.toName()

            val fields = getFields(it,sc)

            val funcs = getFuncs(it,sc)

            val yaModel = YaModel(name,className,fields, funcs, boServiceTable.row(className).values.toList(),it)
            if(map.containsKey(className)||bizMap.containsKey(name)){
                throw Exception("模型业务名称重复!  $className $name")
            }
            map[className] = yaModel
            bizMap[name] = yaModel

        }
        println("yabo 模型初始化完成!")

    }

    private fun getFuncs(c: Class<*>,sc: Class<*>?,type:String = "inner"): List<YaMethod> {

        val allFunc = c.declaredMethods.toMutableList()
        sc?.let {
            allFunc.addAll(it.declaredMethods)
        }

        return allFunc.mapNotNull { f ->
            f.getAnnotation(Func::class.java)?.let { method ->
                val funcName = f.name
                val returnType = f.returnType.toString()
                val parameters = f.parameterTypes.mapNotNull { p -> p.name }
                val pa = f.parameterAnnotations
                var from = ""
                for (i in pa.indices) {
                    val a = pa[i]
                    if(From::class in a.map { it.annotationClass }){
                        //println("get from")
                        from = parameters[i].toName()
                    }
                }

                //println("$fieldName ->  $name2")
                YaMethod(funcName, method.name, type, parameters,returnType, f,from)
            }
        }
    }

    private fun getFields(c: Class<*>,sc: Class<*>?): List<YaField> {

        val allField = c.declaredFields.toMutableList()
        sc?.let {
           allField.addAll(sc.declaredFields.toList() )
        }
        return allField.mapNotNull { f ->
            val fieldName = f.name
            val clazz = f.type
            val a = f.getAnnotation(Field::class.java)
            a?.let { a->
                val name = a.name
                val fType = a.type
                val clazzStr = getClazzStr(clazz, f,fType)
                val type = when (clazz.toString().toName()) {
                    "int", "long", "string", "double", "boolean" -> ClassType.BASE.toString()
                    "list", "set", "map" -> {
                        if ("string int long boolean double".contains(clazzStr.split(" ")[1])) {
                            ClassType.COLL_BASE.toString()
                        } else {
                            ClassType.COLL_BO.toString()
                        }
                    }
                    else -> {
                        if (clazz.getAnnotation(Bo::class.java) != null) {
                            ClassType.BO.toString()
                        } else if (clazz.getAnnotation(Tuple::class.java) != null) {
                            ClassType.TUPLE.toString()
                        } else {
                            "other"
                        }
                    }
                }
                //println("$fieldName ->  $name2")
                    YaField(fieldName, name, type, clazzStr, clazz)
            }
        }
    }

    /**
     * Get clazz str
     *
     * @param clazz
     * @param f
     * @return
     */
    private fun getClazzStr( clazz: Class<*>, f: java.lang.reflect.Field,fType:String):String {
        return when(clazz){
            //泛型处理
            List::class.java,Set::class.java,Map::class.java -> {
                f.isAccessible = true
                val gt = f.genericType
                val genTypes = (gt as ParameterizedType).actualTypeArguments.joinToString(" ") {
                    it.toString().toName()
                }
                clazz.name.toName()+" "+genTypes
            }
            //非泛型处理
            else -> {
                val c = clazz.toString().toName()
                return if(c == "string" && fType != ""){
                     fType
                }else{
                    c
                }
            }
        }
    }
}

fun String.toLow(): String {
    return if (Character.isLowerCase(this[0])) {
        this
    } else {
        Character.toLowerCase(this[0])+(this.substring(1))
    }
}

fun String.toName(): String {
    return this.split(".").last().toLow()
}