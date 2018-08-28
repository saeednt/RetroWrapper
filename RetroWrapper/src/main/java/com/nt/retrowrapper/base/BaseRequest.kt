package com.nt.retrowrapper.base

import com.nt.retrowrapper.Verb

interface BaseRequest<T> {
    val method: Verb
    val url: String
    val resultType: Class<T>
}