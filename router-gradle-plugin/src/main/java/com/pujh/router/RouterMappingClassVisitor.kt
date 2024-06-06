package com.pujh.router

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class RouterMappingClassVisitor(
    private val nextVisitor: ClassVisitor
) : ClassNode(Opcodes.ASM9) {

    override fun visitEnd() {
        super.visitEnd()
        val classInterface = interfaces
        println("privacy transform classNodeName: $this")

//        classInterface?.forEach { interface ->
//            if (Interface == "android/view/View\$OnClickListener") {
//                methods?.forEach { method ->
//                    // 找到onClick 方法
//                    if (method.name == "<init>") {
//                        initFunction(this, method)
//                    }
//                    if (method.name == "onClick" && method.desc == "(Landroid/view/View;)V") {
//                        insertTrack(this, method)
//                        Log.info("Find the method ${method.name} and the desc ${method.desc}")
//
//                    }
//                }
//            }
//        }
        accept(nextVisitor)
    }

    private fun initFunction(node: ClassNode, method: MethodNode) {
        var hasLog = false
        node.fields?.forEach {
            if (it.name == "textLog") {
                hasLog = true
            }
        }

        if (!hasLog) {
            node.visitField(
                Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "textLog", String.format(
                    "L%s;",
                    "com/kotlin/aop/Buried_point/NewtextLog"
                ), node.signature, null
            )
            val instructions = method.instructions
            method.instructions?.iterator()?.forEach {
                if ((it.opcode >= Opcodes.IRETURN && it.opcode <= Opcodes.RETURN) || it.opcode == Opcodes.ATHROW) {
                    instructions.insertBefore(it, VarInsnNode(Opcodes.ALOAD, 0))
                    instructions.insertBefore(
                        it,
                        TypeInsnNode(Opcodes.NEW, "com/kotlin/aop/Buried_point/NewtextLog")
                    )
                    instructions.insertBefore(it, InsnNode(Opcodes.DUP))
                    instructions.insertBefore(
                        it, MethodInsnNode(
                            Opcodes.INVOKESPECIAL, "com/kotlin/aop/Buried_point/NewtextLog",
                            "<init>", "()V", false
                        )
                    )
                    instructions.insertBefore(
                        it, FieldInsnNode(
                            Opcodes.PUTFIELD, node.name, "textLog",
                            String.format("L%s;", "com/kotlin/aop/Buried_point/NewtextLog")
                        )
                    )
                }
            }
        }
    }


    private fun insertTrack(node: ClassNode, method: MethodNode) {
        // 判断方法名和方法描述
        val instructions = method.instructions
        val firstNode = instructions.first
        //Insert the code
        instructions?.insertBefore(firstNode, VarInsnNode(Opcodes.ALOAD, 0))
        instructions?.insertBefore(
            firstNode, FieldInsnNode(
                Opcodes.GETFIELD, node.name,
                "textLog", String.format("L%s;", "com/kotlin/aop/Buried_point/NewtextLog")
            )
        )

        instructions?.insertBefore(
            firstNode, MethodInsnNode(
                Opcodes.INVOKEVIRTUAL, "com/kotlin/aop/Buried_point/NewtextLog",
                "textLog", "()V", false
            )
        )
    }

    companion object {
        private const val PACKAGE_NAME = "com/imooc/router/mapping"
        private const val CLASS_NAME_PREFIX = "RouterMapping_"
        private const val CLASS_FILE_SUFFIX = ".class"
    }
}