plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation("com.android.tools.build:gradle:8.4.1")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.0-1.0.21")
    implementation("org.ow2.asm:asm-util:9.7")
}

gradlePlugin {
    plugins {
        create("router") {
            group = "com.pujh"
            id = "com.pujh.router"
            implementationClass = "com.pujh.router.RouterPlugin"
            version = "1.0"
        }
    }
}