plugins {
    id("org.jetbrains.kotlin.jvm").version(Versions.org_jetbrains_kotlin)
    id("jmfayard.github.io.gradle-kotlin-dsl-libs").version(Versions.jmfayard_github_io_gradle_kotlin_dsl_libs_gradle_plugin)
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)

    testImplementation(Libs.kotlin_test)
    testImplementation(Libs.kotlin_test_junit)
}

application {
    mainClassName = "com.github.ohtomi.kotlin.sandbox.AppKt"
}
