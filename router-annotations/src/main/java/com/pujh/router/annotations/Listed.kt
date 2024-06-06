package com.pujh.router.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Listed(
    val name: String,
)
