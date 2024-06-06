package com.pujh.router

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.ClassVisitor

abstract class RouterClassVisitorFactory : AsmClassVisitorFactory<RouterParameters> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val wikiDir = parameters.get().wikiDir.get()
        println("parameters wikiDir = $wikiDir")
//        return TraceClassVisitor(nextClassVisitor, PrintWriter(System.out))
        return RouterMappingClassVisitor(nextClassVisitor)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className
        return className.startsWith("com.pujh.router.mapping.RouterMapping_")
    }
}