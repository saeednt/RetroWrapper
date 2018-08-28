package com.nt.retrowrappercompiler

import com.google.auto.service.AutoService
import com.nt.retrowrappercompiler.processor.CallInterface
import com.nt.retrowrappercompiler.processor.CallerClass
import com.nt.retrowrappercompiler.processor.RetrofitWrapper
import com.nt.retrowrapper.annots.Request
import com.nt.retrowrapper.annots.RetroConfig
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@AutoService(Processor::class)
class RWrapper : AbstractProcessor() {
    var retroConfigClass: TypeMirror? = null
    lateinit var config: RetroConfig
    var retroConfigPack: PackageElement? = null

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Request::class.java.name)
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Processing @Request annotations...")

        val configClasses = p1?.getElementsAnnotatedWith(RetroConfig::class.java)
        if (configClasses == null) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "You are missing config class. Please annotate one of your classes with @RetroConfig and implement the intended config for retrofit")
            return true
        }

        if (configClasses.isEmpty() || configClasses.size == 1) {
            configClasses.forEach {
                config = it.getAnnotation(RetroConfig::class.java)
                retroConfigClass = it.asType()
                retroConfigPack = processingEnv.elementUtils.getPackageOf(it)
                RetrofitWrapper.generate(processingEnv, retroConfigClass!!, retroConfigPack!!)
                return@forEach
            }
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "You should only annotate exactly one class with @RetroConfig")
            return true
        }

        p1.getElementsAnnotatedWith(Request::class.java)?.forEach {
            val pack = processingEnv.elementUtils.getPackageOf(it)
            val requestAnnotation = it.getAnnotation(Request::class.java)

            CallInterface.generate(
                    processingEnv,
                    config,
                    it,
                    pack,
                    requestAnnotation
            )
            CallerClass.generate(
                    processingEnv,
                    retroConfigPack!!,
                    retroConfigClass!!,
                    it,
                    pack,
                    CallInterface.callInterface,
                    CallInterface.callFunction,
                    requestAnnotation
            )
        }

        return true
    }



    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}