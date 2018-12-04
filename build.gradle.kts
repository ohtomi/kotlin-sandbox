plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.10")
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.10")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.10")
}

application {
    mainClassName = "com.github.ohtomi.kotlin.sandbox.AppKt"
}
