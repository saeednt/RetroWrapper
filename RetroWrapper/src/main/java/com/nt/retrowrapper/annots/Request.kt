package com.nt.retrowrapper.annots

import com.nt.retrowrapper.Verb
import javax.lang.model.type.NullType
import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Request(
        val url: String,
        val verb: Verb,
        val bodyType: KClass<*> = NullType::class,
        val returnType: KClass<*>,
        val rxEnabled: Boolean = true,
        val pagedRequest: Boolean = false,
        val ordered: Boolean = false,
        val hasQueryMap: Boolean = false
)