package com.nt.retrowrapper.annots

import javax.lang.model.type.NullType
import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CRUDRequest(
        val url: String,
        val entity: KClass<*>,
        val request: KClass<*> = NullType::class
)