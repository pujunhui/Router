package com.pujh.router

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.INVOKEINTERFACE
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.V1_7

object RouterMappingByteCodeBuilder : Opcodes {
    private const val CLASS_NAME = "com/pujh/router/mapping/generated/RouterMapping"

    fun `get`(allMappingNames: Set<String>): ByteArray {
        // 1. 创建一个类
        // 2. 创建构造方法
        // 3. 创建get方法
        //   （1）创建一个Map
        //   （2）塞入所有映射表的内容
        //   （3）返回map

        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)

        cw.visit(
            V1_7,
            ACC_PUBLIC + ACC_SUPER,
            CLASS_NAME,
            null,
            "java/lang/Object",
            null
        )

        // 生成或者编辑方法
        var mv: MethodVisitor

        // 创建构造方法
        mv = cw.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>",
            "()V",
            null,
            null
        )

        mv.visitCode()
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object", "<init>", "()V", false
        )
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()

        // 创建get方法
        mv = cw.visitMethod(
            ACC_PUBLIC + ACC_STATIC,
            "get",
            "()Ljava/util/Map;",
            "()Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;",
            null
        )

        mv.visitCode()

        mv.visitTypeInsn(NEW, "java/util/HashMap")
        mv.visitInsn(DUP)
        mv.visitMethodInsn(
            INVOKESPECIAL,
            "java/util/HashMap", "<init>", "()V", false
        )
        mv.visitVarInsn(ASTORE, 0)

        // 向Map中，逐个塞入所有映射表的内容
        allMappingNames.forEach {
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(
                INVOKESTATIC,
                "com/pujh/router/mapping/$it",
                "get", "()Ljava/util/Map;", false
            )
            mv.visitMethodInsn(
                INVOKEINTERFACE,
                "java/util/Map",
                "putAll",
                "(Ljava/util/Map;)V", true
            )
        }

        // 返回map
        mv.visitVarInsn(ALOAD, 0)
        mv.visitInsn(ARETURN)
        mv.visitMaxs(2, 2)

        mv.visitEnd()

        return cw.toByteArray()
    }
}