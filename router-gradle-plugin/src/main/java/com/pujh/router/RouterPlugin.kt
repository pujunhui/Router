package com.pujh.router

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import java.io.File

class RouterPlugin : Plugin<Project> {

    // 实现apply方法，注入插件的逻辑
    override fun apply(project: Project) {
        if (project.plugins.hasPlugin(AppPlugin::class.java)) {
            val extension = project.extensions.create<RouterExtension>(EXTENSION_NAME)

            val androidComponents =
                project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->
                variant.instrumentation.transformClassesWith(
                    RouterClassVisitorFactory::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.wikiDir.set(extension.wikiDir)
                }
                variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
            }
        }

        // 1. 自动帮助用户传递路径参数到注解处理器中
        project.extensions.findByType(KspExtension::class.java)?.apply {
            arg("root_project_dir", project.rootProject.projectDir.absolutePath)
            arg("route_module_name", project.name)
        }

        // 2. 实现旧的构建产物的自动清理
        project.tasks.findByName("clean")?.doFirst {
            // 删除 上一次构建生成的 router_mapping目录
            val routerMappingDir = File(project.rootProject.projectDir, "router_mapping")
            if (routerMappingDir.exists()) {
                routerMappingDir.deleteRecursively()
            }
        }

        if (!project.plugins.hasPlugin(AppPlugin::class.java)) {
            return
        }
    }

    companion object {
        const val EXTENSION_NAME: String = "router"
    }
}