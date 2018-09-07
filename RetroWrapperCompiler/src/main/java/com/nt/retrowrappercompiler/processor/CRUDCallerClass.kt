package com.nt.retrowrappercompiler.processor

import com.nt.retrowrapper.annots.CRUDRequest
import com.nt.retrowrapper.base.ProgressHandler
import com.nt.retrowrappercompiler.RWrapper
import com.squareup.kotlinpoet.*
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.io.File
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

    private lateinit var apiCallCodeBlock: CodeBlock.Builder

    private lateinit var getListImplementation: FunSpec.Builder


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

        generateApiCallCodeBlock()

        generateProperties()
        generateFunctions()

        addFunctionsToClass()

        createFile()
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

    private fun generateApiCallCodeBlock() {
        apiCallCodeBlock = CodeBlock.builder()
        apiCallCodeBlock
                .add(".observeOn(obsScheduler)\n")
                .add(".subscribeOn(%T.io())\n", Schedulers::class)
                .add(".doOnSubscribe{ progress?.showProgress() }\n")
                .add(".doOnError{ progress?.showError() }\n")
                .add(".doOnNext{ progress?.hideProgress() }\n")
    }


    private lateinit var getFunImplementation: FunSpec.Builder
    private lateinit var postFunImplementation: FunSpec.Builder
    private lateinit var putFunImplementation: FunSpec.Builder
    private lateinit var deleteFunImplementation: FunSpec.Builder

    private fun generateFunctions() {
        var getListBuild = getListFun.build()
        var getFunBuild = getFun.build()
        var postFunBuild = postFun.build()
        var putFunBuild = putFun.build()
        var deleteFunBuild = deleteFun.build()

        this.getListImplementation = FunSpec.builder(getListBuild.name).returns(getListBuild.returnType!!).addParameters(getListBuild.parameters).addParameter(ParameterSpec.builder(progressParameter.name, ProgressHandler::class.asTypeName().asNullable()).build()).addCode("this.${progressParameter.name} = ${progressParameter.name}\n")
                .addCode("return api.getList()\n")
                .addCode(apiCallCodeBlock.build())
        this.getFunImplementation = FunSpec.builder(getFunBuild.name).returns(getFunBuild.returnType!!).addParameters(getFunBuild.parameters).addParameter(ParameterSpec.builder(progressParameter.name, ProgressHandler::class.asTypeName().asNullable()).build()).addCode("this.${progressParameter.name} = ${progressParameter.name}\n")
                .addCode("return api.get(")
        getFunBuild.parameters.forEachIndexed { index, spec ->
            this.getFunImplementation.addCode(spec.name)
            if (index < getFunBuild.parameters.size - 1) {
                this.getFunImplementation.addCode(", ")
            }
        }
        this.getFunImplementation.addCode(")")
                .addCode(apiCallCodeBlock.build())

        this.postFunImplementation = FunSpec.builder(postFunBuild.name).returns(postFunBuild.returnType!!).addParameters(postFunBuild.parameters).addParameter(ParameterSpec.builder(progressParameter.name, ProgressHandler::class.asTypeName().asNullable()).build()).addCode("this.${progressParameter.name} = ${progressParameter.name}\n")
                .addCode("return api.post(")
        postFunBuild.parameters.forEachIndexed { index, spec ->
            this.postFunImplementation.addCode(spec.name)
            if (index < postFunBuild.parameters.size - 1) {
                this.postFunImplementation.addCode(", ")
            }
        }
        this.postFunImplementation.addCode(")")
                .addCode(apiCallCodeBlock.build())

        this.putFunImplementation = FunSpec.builder(putFunBuild.name).returns(putFunBuild.returnType!!).addParameters(putFunBuild.parameters).addParameter(ParameterSpec.builder(progressParameter.name, ProgressHandler::class.asTypeName().asNullable()).build()).addCode("this.${progressParameter.name} = ${progressParameter.name}\n")
                .addCode("return api.put(")
        putFunBuild.parameters.forEachIndexed { index, spec ->
            this.putFunImplementation.addCode(spec.name)
            if (index < putFunBuild.parameters.size - 1) {
                this.putFunImplementation.addCode(", ")
            }
        }
        this.putFunImplementation.addCode(")")
                .addCode(apiCallCodeBlock.build())

        this.deleteFunImplementation = FunSpec.builder(deleteFunBuild.name).returns(deleteFunBuild.returnType!!).addParameters(deleteFunBuild.parameters).addParameter(ParameterSpec.builder(progressParameter.name, ProgressHandler::class.asTypeName().asNullable()).build()).addCode("this.${progressParameter.name} = ${progressParameter.name}\n")
                .addCode("return api.delete(")
        deleteFunBuild.parameters.forEachIndexed { index, spec ->
            this.deleteFunImplementation.addCode(spec.name)
            if (index < deleteFunBuild.parameters.size - 1) {
                this.deleteFunImplementation.addCode(", ")
            }
        }
        this.deleteFunImplementation.addCode(")")
                .addCode(apiCallCodeBlock.build())
    }


    private fun addFunctionsToClass() {
        callerClassBuilder.addFunction(this.getListImplementation.build())
        callerClassBuilder.addFunction(this.getFunImplementation.build())
        callerClassBuilder.addFunction(this.postFunImplementation.build())
        callerClassBuilder.addFunction(this.putFunImplementation.build())
        callerClassBuilder.addFunction(this.deleteFunImplementation.build())
    }

    private fun createFile() {
        callerClassFileBuilder.addType(callerClassBuilder.build())
        val kaptKotlinGeneratedDir = processingEnv.options[RWrapper.KAPT_KOTLIN_GENERATED_OPTION_NAME]
        callerClassFileBuilder.build().writeTo(File(kaptKotlinGeneratedDir, "$callerClassName.kt"))
    }

}
