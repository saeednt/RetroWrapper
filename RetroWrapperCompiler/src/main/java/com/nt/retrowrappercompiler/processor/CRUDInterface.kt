package com.nt.retrowrappercompiler.processor

import com.nt.retrowrapper.annots.CRUDRequest
import com.nt.retrowrapper.annots.RetroConfig
import com.nt.retrowrappercompiler.RWrapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.type.MirroredTypeException


internal object CRUDInterface {
    private lateinit var processingEnv: ProcessingEnvironment
    private lateinit var config: RetroConfig
    private lateinit var element: Element
    private lateinit var request: CRUDRequest

    private lateinit var crudInterfaceFileName: String
    private lateinit var crudInterfaceFileBuilder: FileSpec.Builder
    lateinit var crudInterfaceBuilder: TypeSpec.Builder

    lateinit var getFun: FunSpec.Builder
    lateinit var getListFun: FunSpec.Builder
    lateinit var postFun: FunSpec.Builder
    lateinit var putFun: FunSpec.Builder
    lateinit var deleteFun: FunSpec.Builder

    fun generate(processingEnv: ProcessingEnvironment, config: RetroConfig, it: Element, pack: PackageElement, crudRequest: CRUDRequest) {
        this.processingEnv = processingEnv
        this.config = config
        this.element = it
        this.request = crudRequest

        crudInterfaceFileName = it.simpleName.toString() + "Caller"
        crudInterfaceFileBuilder = FileSpec.builder(pack.toString(), crudInterfaceFileName)
        crudInterfaceBuilder = TypeSpec.interfaceBuilder(crudInterfaceFileName)

        generateCrudFunctions()
        generateReturnTypes()
        generateAnnotations()
        generateParams()

        addFunctionsToInterface()
        createFile()
    }

    private fun generateCrudFunctions() {
        this.getFun = FunSpec.builder("get")
        this.getListFun = FunSpec.builder("getList")
        this.postFun = FunSpec.builder("post")
        this.putFun = FunSpec.builder("put")
        this.deleteFun = FunSpec.builder("delete")
    }

    private fun generateReturnTypes() {
        try {
            request.entity
        } catch (e: MirroredTypeException) {
            this.getFun.returns(Observable::class.asTypeName().parameterizedBy(e.typeMirror.asTypeName()))
            this.getListFun.returns(Observable::class.asTypeName().parameterizedBy(List::class.asTypeName().parameterizedBy(e.typeMirror.asTypeName())))
        }
        this.postFun.returns(Observable::class.asTypeName().parameterizedBy(ResponseBody::class.asTypeName()))
        this.putFun.returns(Observable::class.asTypeName().parameterizedBy(ResponseBody::class.asTypeName()))
        this.deleteFun.returns(Observable::class.asTypeName().parameterizedBy(ResponseBody::class.asTypeName()))
    }

    private fun generateAnnotations() {
        this.getListFun.addAnnotation(AnnotationSpec.builder(GET::class).addMember(request.url).build())
        this.getFun.addAnnotation(AnnotationSpec.builder(GET::class).addMember(request.url + "/{id}").build())
        this.postFun.addAnnotation(AnnotationSpec.builder(GET::class).addMember(request.url + "/{id}").build())
        this.putFun.addAnnotation(AnnotationSpec.builder(GET::class).addMember(request.url + "/{id}").build())
        this.deleteFun.addAnnotation(AnnotationSpec.builder(GET::class).addMember(request.url + "/{id}").build())
    }

    private fun generateParams() {
        this.getFun.addParameter(ParameterSpec.builder("id", Int::class).addAnnotation(AnnotationSpec.builder(Path::class).addMember("id").build()).build())
        this.postFun.addParameter(ParameterSpec.builder("id", Int::class).addAnnotation(AnnotationSpec.builder(Path::class).addMember("id").build()).build())
        this.putFun.addParameter(ParameterSpec.builder("id", Int::class).addAnnotation(AnnotationSpec.builder(Path::class).addMember("id").build()).build())
        this.deleteFun.addParameter(ParameterSpec.builder("id", Int::class).addAnnotation(AnnotationSpec.builder(Path::class).addMember("id").build()).build())
    }

    private fun addFunctionsToInterface() {
        crudInterfaceBuilder.addFunction(this.getFun.build())
        crudInterfaceBuilder.addFunction(this.getListFun.build())
        crudInterfaceBuilder.addFunction(this.postFun.build())
        crudInterfaceBuilder.addFunction(this.putFun.build())
        crudInterfaceBuilder.addFunction(this.deleteFun.build())
    }

    private fun createFile() {
        crudInterfaceFileBuilder.addType(crudInterfaceBuilder.build())
        val kaptKotlinGeneratedDir = processingEnv.options[RWrapper.KAPT_KOTLIN_GENERATED_OPTION_NAME]
        crudInterfaceFileBuilder.build().writeTo(File(kaptKotlinGeneratedDir, "${crudInterfaceFileName}.kt"))
    }
}