package com.nt.retrowrapper.base

open class BaseCaller {
    var page = 0
        get() {
            field += 1
            return field - 1
        }


    var pageSize = 10
    var order = "asc"
    var orderBy = "id"
}