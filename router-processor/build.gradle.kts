plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.symbol.processing.api)

    implementation(project(":router-annotations"))
    implementation("com.squareup:kotlinpoet:1.17.0")
    implementation("com.squareup:kotlinpoet-ksp:1.17.0")
    implementation("com.google.code.gson:gson:2.11.0")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}