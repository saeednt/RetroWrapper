package com.nt.retrowrappercompiler.processor

import com.nt.retrowrappercompiler.RWrapper
import com.squareup.kotlinpoet.*
import retrofit2.Retrofit
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.PackageElement
import javax.lang.model.type.TypeMirror

internal object RetrofitWrapper {
    internal fun generate(processingEnv: ProcessingEnvironment, retroConfigClass: TypeMirror, pack: PackageElement) {
        val fileName = "RetroKeeper"
        val fileBuilder = FileSpec.builder(pack.toString(), fileName)
        val classBuilder = TypeSpec.objectBuilder(fileName)

        val initializerBlock = CodeBlock.builder()
        initializerBlock
                .add("lazy{\n")
                .add("val rconfig = %T()\n", retroConfigClass)
                .add("val r = %T()\n", Retrofit.Builder::class.java)
        initializerBlock.add("r.baseUrl(rconfig.baseUrl)\n")
        initializerBlock.add("rconfig.converters.forEach({" +
                "r.addConverterFactory(it)" +
                "})\n")
        initializerBlock.add("rconfig.callAdapters.forEach({" +
                "r.addCallAdapterFactory(it)" +
                "})\n")
        initializerBlock.add("r.build()\n}")
        classBuilder.addProperty(PropertySpec.builder("retro", Retrofit::class.java, KModifier.PUBLIC).mutable(false).delegate(initializerBlock.build()).build())

        fileBuilder.addType(classBuilder.build())
        val kaptKotlinGeneratedDir = processingEnv.options[RWrapper.KAPT_KOTLIN_GENERATED_OPTION_NAME]
        fileBuilder.build().writeTo(File(kaptKotlinGeneratedDir, "$fileName.kt"))
    }
}