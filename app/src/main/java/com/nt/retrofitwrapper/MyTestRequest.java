package com.nt.retrofitwrapper;

import com.nt.retrowrapper.Verb;
import com.nt.retrowrapper.annots.Request;

@Request(
        url = "usr",
        verb = Verb.GET,
        returnType = MyTestRequest.class
)
interface MyTestRequest {
}
