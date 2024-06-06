package com.pujh.router

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface RouterParameters : InstrumentationParameters {
    @get:Input
    val wikiDir: Property<String>
}