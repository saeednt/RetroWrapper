package com.nt.retrofitwrapper

import com.nt.retrowrapper.annots.CRUDRequest

@CRUDRequest(
        url = "user",
        entity = User::class,
        request = User::class
)
interface GetUserProfile