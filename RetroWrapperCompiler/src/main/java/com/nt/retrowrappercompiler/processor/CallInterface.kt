package com.nt.retrowrappercompiler.processor

import com.nt.retrowrapper.Verb
import com.nt.retrowrapper.annots.Field
import com.nt.retrowrapper.annots.Request
import com.nt.retrowrapper.annots.RetroConfig
import com.nt.retrowrappercompiler.RWrapper
import com.squareup.kotlinpoet.*
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.NullType
import javax.tools.Diagnostic
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

import kotlin.reflect.full.companionObject

internal object CallInterface {
    private lateinit var processingEnv: ProcessingEnvironment
    // Environment classes for common configs of api
    private lateinit var config: RetroConfig

    //Builders for making the call interface
    private lateinit var element: Element
    private lateinit var callInterfaceFileName: String
    private lateinit var callInterfaceFileBuilder: FileSpec.Builder
    private lateinit var interfaceBuilder: TypeSpec.Builder
    private lateinit var callFunctionBuilder: FunSpec.Builder

    //The fully built interface and function
    lateinit var callInterface: TypeSpec
    lateinit var callFunction: FunSpec

    internal fun generate(processingEnv: ProcessingEnvironment, config: RetroConfig, it: Element?, callPackage: PackageElement?, requestAnnotation: Request) {
        this.processingEnv = processingEnv
        this.config = config
        this.element = it!!

        callInterfaceFileName = it.simpleName.toString() + "Call"
        callInterfaceFileBuilder = FileSpec.builder(callPackage.toString(), callInterfaceFileName)
        interfaceBuilder = TypeSpec.interfaceBuilder(callInterfaceFileName)
        callFunctionBuilder = FunSpec.builder("call")

        generateHttpMethod(callFunctionBuilder, requestAnnotation)
        generateReturnType(callFunctionBuilder, requestAnnotation)

        callFunctionBuilder.addModifiers(KModifier.ABSTRACT)

        callFunction = callFunctionBuilder.build()
        interfaceBuilder.addFunction(callFunction)

        callInterface = interfaceBuilder.build()
        callInterfaceFileBuilder.addType(callInterface)

        val kaptKotlinGeneratedDir = processingEnv.options[RWrapper.KAPT_KOTLIN_GENERATED_OPTION_NAME]
        callInterfaceFileBuilder.build().writeTo(File(kaptKotlinGeneratedDir, "$callInterfaceFileName.kt"))

    }

    private fun generateHttpMethod(funSpec: FunSpec.Builder, requestAnnotation: Request) {
            when (requestAnnotation.verb) {
                Verb.GET -> {
                    funSpec.addAnnotation(AnnotationSpec.builder(retrofit2.http.GET::class.java).addMember("%S", requestAnnotation.url).build())
                    if (requestAnnotation.url.contains("{")) {
                        generateGetParams(funSpec, requestAnnotation.url, requestAnnotation)
                    }
                }
                Verb.POST -> {
                    funSpec.addAnnotation(AnnotationSpec.builder(retrofit2.http.POST::class.java).addMember("%S", requestAnnotation.url).build())
                    generatePostParams(funSpec, requestAnnotation)
                }
                else -> {
                }
            }

    }

    private fun generateReturnType(funSpec: FunSpec.Builder, requestAnnotation: Request) {
            try {
                requestAnnotation.returnType
            } catch (e: MirroredTypeException) {
                if (!requestAnnotation.rxEnabled) {
                    funSpec.returns(Call::class.asClassName().parameterizedBy((processingEnv.typeUtils.asElement(e.typeMirror) as TypeElement).asClassName()))
                } else {
                    funSpec.returns(Observable::class.asClassName().parameterizedBy((processingEnv.typeUtils.asElement(e.typeMirror) as TypeElement).asClassName()))
                }
            }
    }

    private fun generatePostParams(funSpec: FunSpec.Builder, requestAnnotation: Request) {
        val fieldAnnots = element.getAnnotationsByType(Field::class.java)
        if (fieldAnnots.any()){
            generateFormParams(fieldAnnots, funSpec)
            return
        }

        try {
            requestAnnotation.bodyType
        } catch (e: MirroredTypeException) {
            if(e.typeMirror == NullType::class) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "You must specify body type of request in annotation.")
                throw e
            }
            val typeElement = processingEnv.typeUtils.asElement(e.typeMirror) as TypeElement
            if (typeElement == NullType::class) {
                return
            }
            funSpec.addParameter(ParameterSpec.builder("body", (typeElement.asClassName())).addAnnotation(Body::class).build())
        }
    }

    private fun generateFormParams(fieldAnnots: Array<out Field>?, funSpec: FunSpec.Builder) {
        funSpec.addAnnotation(FormUrlEncoded::class)
        fieldAnnots?.forEach {
            funSpec.addParameter(ParameterSpec.builder(it.name, String::class).addAnnotation(retrofit2.http.Field::class).build())
        }
    }

    private fun generateGetParams(funSpec: FunSpec.Builder, url: String, requestAnnotation: Request) {
        val i = 0
        var indexOf = url.indexOf("{", i, true)
        while (indexOf > 0) {
            val closeIndex = url.indexOf("}", indexOf, true)
            val substring = url.substring(indexOf + 1, closeIndex)
            funSpec.addParameter(ParameterSpec
                    .builder(substring, String::class)
                    .addAnnotation(AnnotationSpec.builder(Path::class).addMember("%S", substring).build())
                    .build())
            indexOf = url.indexOf("{", closeIndex, true)
        }

        if (requestAnnotation.hasQueryMap) {
            funSpec.addParameter(
                    ParameterSpec.builder("queryMap", Map::class.asClassName().parameterizedBy(String::class.asTypeName(), String::class.asTypeName()))
                            .addAnnotation(AnnotationSpec.builder(QueryMap::class.java).build())
                            .build()
            )
        }

        if (requestAnnotation.pagedRequest) {
            funSpec.addParameter(ParameterSpec.builder("page", Int::class.java).addAnnotation(AnnotationSpec.builder(Query::class.java).addMember("%S", config.pageName).build()).build())
            funSpec.addParameter(ParameterSpec.builder("pageSize", Int::class.java).addAnnotation(AnnotationSpec.builder(Query::class.java).addMember("%S", config.pageSizeName).build()).build())
        }
        if (requestAnnotation.ordered) {
            funSpec.addParameter(ParameterSpec.builder("order", Int::class.java).addAnnotation(AnnotationSpec.builder(Query::class.java).addMember("%S", config.order).build()).build())
            funSpec.addParameter(ParameterSpec.builder("orderBy", Int::class.java).addAnnotation(AnnotationSpec.builder(Query::class.java).addMember("%S", config.orderBy).build()).build())
        }

    }
}