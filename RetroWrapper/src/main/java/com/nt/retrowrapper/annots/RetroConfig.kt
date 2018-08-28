package com.nt.retrowrapper.annots

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RetroConfig(
        val pageName: String = "page",
        val pageSizeName: String = "pageSize",
        val order: String = "order",
        val orderBy: String = "orderBy"
)