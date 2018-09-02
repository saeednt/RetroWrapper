package com.nt.retrofitwrapper

import com.nt.retrowrapper.Verb
import com.nt.retrowrapper.annots.Request

@Request(
        url = "user/{username}/profile",
        verb = Verb.GET,
        returnType = User::class,
        rxEnabled = false
)
interface GetUserProfile