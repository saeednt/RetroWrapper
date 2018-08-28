package com.nt.retrowrappercompiler.processor

import com.nt.retrowrapper.annots.Request
import com.nt.retrowrapper.base.BaseCaller
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

internal object CallerClass {
    private lateinit var processingEnv: ProcessingEnvironment

    private lateinit var callerFileName: String
    private lateinit var callerFileBuilder: FileSpec.Builder
    private lateinit var callerClassBuilder: TypeSpec.Builder
    private lateinit var abstractCallFunction: FunSpec
    private lateinit var progressParameter: ParameterSpec
    private lateinit var callFunctionBuilder: FunSpec.Builder
    private lateinit var apiCallCodeBlock: CodeBlock.Builder

    internal fun generate(processingEnv: ProcessingEnvironment, retroConfigPack: PackageElement, retroConfigClass: TypeMirror, it: Element?, pack: PackageElement?, callInterface: TypeSpec, abstractCallFunction: FunSpec, requestAnnotation: Request) {
        CallerClass.processingEnv = processingEnv
        CallerClass.abstractCallFunction = abstractCallFunction

        callerFileName = it?.simpleName.toString() + "Caller"
        callerFileBuilder = FileSpec.builder(pack.toString(), callerFileName)
        callerClassBuilder = TypeSpec.classBuilder(callerFileName)

        if (requestAnnotation.pagedRequest)
            callerClassBuilder.superclass(BaseCaller::class.java)

        progressParameter = ParameterSpec.builder("progress", ProgressHandler::class.asTypeName().asNullable()).defaultValue("null").build()

        generateCallerProperties(pack, callInterface, retroConfigPack, retroConfigClass)

        generateApiCallCodeBlock()

        generateCallFunction()

        if (requestAnnotation.pagedRequest) {
            generateNextPageFunction()
            generateRefreshFunction()
        }

        callerFileBuilder.addType(callerClassBuilder.build())
        val kaptKotlinGeneratedDir = processingEnv.options[RWrapper.KAPT_KOTLIN_GENERATED_OPTION_NAME]
        callerFileBuilder.build().writeTo(File(kaptKotlinGeneratedDir, "$callerFileName.kt"))
    }

    private fun generateCallerProperties(pack: PackageElement?, callInterface: TypeSpec, retroConfigPack: PackageElement, retroConfigClass: TypeMirror) {
        callerClassBuilder.addProperty(PropertySpec.builder("api", ClassName(pack.toString(), callInterface.name!!))
                .mutable(false)
                .addModifiers(KModifier.PRIVATE)
                .initializer("%1T.retro.create(%2T::class.java)", ClassName(retroConfigPack.toString(), "RetroKeeper"), ClassName(pack.toString(), callInterface.name!!))
                .build())

        callerClassBuilder.addProperty(
                PropertySpec.builder("obsScheduler", Scheduler::class.java)
                        .mutable(false)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("%T().observeScheduler", retroConfigClass)
                        .build()
        )

        abstractCallFunction.parameters
                .filterNot {
                    it.name.equals("page")
                            || it.name.equals("pageSize")
                            || it.name.equals("order")
                            || it.name.equals("orderBy")
                }
                .forEach {
                    callerClassBuilder.addProperty(
                            PropertySpec.builder(it.name, it.type, KModifier.PRIVATE, KModifier.LATEINIT).mutable(true).build()
                    )
                }

        callerClassBuilder.addProperty(PropertySpec.builder(progressParameter.name, progressParameter.type.asNullable(), KModifier.PRIVATE).mutable(true).initializer("null").build())

    }

    private fun generateApiCallCodeBlock() {
        apiCallCodeBlock = CodeBlock.builder()
        apiCallCodeBlock
                .add("return api.call(")

        abstractCallFunction.parameters
                .forEachIndexed { index, parameterSpec ->
                    apiCallCodeBlock.add(parameterSpec.name)
                    if (index < abstractCallFunction.parameters.size - 1) {
                        apiCallCodeBlock.add(",\n")
                    }
                }
        apiCallCodeBlock
                .add(")\n")
                .add(".observeOn(obsScheduler)\n")
                .add(".subscribeOn(%T.io())\n", Schedulers::class)
                .add(".doOnSubscribe{ progress?.showProgress() }\n")
                .add(".doOnError{ progress?.showError() }\n")
                .add(".doOnNext{ progress?.hideProgress() }\n")
    }

    private fun generateCallFunction() {
        callFunctionBuilder = FunSpec.builder(abstractCallFunction.name)
                .addModifiers(abstractCallFunction.modifiers.minus(KModifier.ABSTRACT))
                .addParameters(abstractCallFunction.parameters.filterNot {
                    it.name.equals("page")
                            || it.name.equals("pageSize")
                            || it.name.equals("order")
                            || it.name.equals("orderBy")
                })
                .returns(abstractCallFunction.returnType!!)
        callFunctionBuilder.addParameter(progressParameter)

        val callFunctionBody = CodeBlock.builder()

        abstractCallFunction.parameters
                .filterNot {
                    it.name.equals("page")
                            || it.name.equals("pageSize")
                            || it.name.equals("order")
                            || it.name.equals("orderBy")
                }
                .forEach {
                    callFunctionBody.add("this.")
                            .add(it.name)
                            .add(" = ")
                            .add(it.name)
                    callFunctionBody.add("\n")
                }
        callFunctionBody.add("this.")
                .add(progressParameter.name)
                .add(" = ")
                .add(progressParameter.name)
                .add("\n")

        callFunctionBody.add(apiCallCodeBlock.build())

        callFunctionBuilder.addCode(
                callFunctionBody.build()
        )

        callerClassBuilder.addFunction(
                callFunctionBuilder.build()
        )
    }

    private fun generateNextPageFunction() {
        callerClassBuilder.addFunction(
                FunSpec.builder("nextPage").addCode(apiCallCodeBlock.build()).returns(abstractCallFunction.returnType!!).build()
        )
    }

    private fun generateRefreshFunction() {
        val refreshBuilder = FunSpec.builder("refresh")
        refreshBuilder
                .addCode("page = 0\n")
                .addCode(apiCallCodeBlock.build())
                .returns(abstractCallFunction.returnType!!)
        callerClassBuilder.addFunction(
                refreshBuilder.build()
        )
    }

}