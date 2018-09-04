package com.nt.retrowrappercompiler.processor

import com.nt.retrowrapper.annots.CRUDRequest
import com.nt.retrowrapper.base.ProgressHandler
import com.squareup.kotlinpoet.*
import io.reactivex.Scheduler
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.PackageElement
import javax.lang.model.type.TypeMirror

internal object CRUDCallerClass {
    private lateinit var processingEnv: ProcessingEnvironment
    private lateinit var retroConfigPack: PackageElement
    private lateinit var retroConfigClass: TypeMirror
    private lateinit var element: Element
    private lateinit var crudRequest: CRUDRequest

    private lateinit var pack: PackageElement

    private lateinit var interfaceBuilder: TypeSpec
    private lateinit var getFun: FunSpec.Builder
    private lateinit var getListFun: FunSpec.Builder
    private lateinit var postFun: FunSpec.Builder
    private lateinit var putFun: FunSpec.Builder
    private lateinit var deleteFun: FunSpec.Builder

    private lateinit var callerClassName: String
    private lateinit var callerClassFileBuilder: FileSpec.Builder
    private lateinit var callerClassBuilder: TypeSpec.Builder

    private lateinit var progressParameter: ParameterSpec

    fun generate(processingEnv: ProcessingEnvironment, retroConfigPack: PackageElement, retroConfigClass: TypeMirror, it: Element, pack: PackageElement,
                 crudInterface: TypeSpec.Builder, getFun: FunSpec.Builder, listFun: FunSpec.Builder, postFun: FunSpec.Builder, putFun: FunSpec.Builder, deleteFun: FunSpec.Builder, crudRequest: CRUDRequest) {
        this.processingEnv = processingEnv
        this.retroConfigPack = retroConfigPack
        this.retroConfigClass = retroConfigClass

        this.element = it
        this.pack = pack

        this.interfaceBuilder = crudInterface.build()
        this.getFun = getFun
        this.getListFun = listFun
        this.postFun = postFun
        this.putFun = putFun
        this.deleteFun = deleteFun

        this.crudRequest = crudRequest

        progressParameter = ParameterSpec.builder("progress", ProgressHandler::class.asTypeName().asNullable()).defaultValue("null").build()

        callerClassName = it.simpleName.toString() + "Caller"
        callerClassFileBuilder = FileSpec.builder(pack.toString(), callerClassName)
        callerClassBuilder = TypeSpec.classBuilder(callerClassName)

        generateProperties()
    }

    private fun generateProperties() {
        callerClassBuilder.addProperty(
                PropertySpec.builder("api", ClassName(pack.toString(), interfaceBuilder.name!!))
                        .mutable(false)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("%1T.retro.create(%2T::class.java)", ClassName(retroConfigPack.toString(), "RetroKeeper"), ClassName(pack.toString(), interfaceBuilder.name!!))
                        .build()
        )
        callerClassBuilder.addProperty(
                PropertySpec.builder("obsScheduler", Scheduler::class.java)
                        .mutable(false)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("%T().observeScheduler", retroConfigClass)
                        .build()
        )

        callerClassBuilder.addProperty(PropertySpec.builder(progressParameter.name, progressParameter.type.asNullable(), KModifier.PRIVATE).mutable(true).initializer("null").build())

    }

}
