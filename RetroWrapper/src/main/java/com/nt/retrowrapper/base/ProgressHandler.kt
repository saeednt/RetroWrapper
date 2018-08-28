package com.nt.retrowrapper.base

interface ProgressHandler {
    fun showProgress()

    fun showError(){}

    fun hideProgress()
}