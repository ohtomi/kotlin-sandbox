import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.kotlin.jvm").version(Versions.org_jetbrains_kotlin)
    id("org.jetbrains.dokka").version(Versions.org_jetbrains_dokka_gradle_plugin)
    id("jmfayard.github.io.gradle-kotlin-dsl-libs").version(Versions.jmfayard_github_io_gradle_kotlin_dsl_libs_gradle_plugin)
    application
    id("org.ajoberstar.reckon").version(Versions.org_ajoberstar_reckon_gradle_plugin)
    id("com.jfrog.bintray").version(Versions.com_jfrog_bintray_gradle_plugin)
    `maven-publish`
}

repositories {
    jcenter()
}

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)

    testImplementation(Libs.kotlin_test)
    testImplementation(Libs.kotlin_test_junit)
}


// https://github.com/ajoberstar/reckon
reckon {
    scopeFromProp()
    stageFromProp("beta", "rc", "final")
}

val props = Props(project)

val bintrayDryRun = true
val bintrayPublish = true
val bintrayOverride = false


val jar by tasks.existing(Jar::class) {
    manifest(closureOf<Manifest> {
        attributes(mapOf(
                "Main-Class" to props.manifestMainClass,
                "Implementation-Title" to props.manifestImplementationTitle,
                "Implementation-Version" to props.manifestImplementationVersion
        ))
    })
}

// https://github.com/Kotlin/dokka
val dokka by tasks.existing(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = "$buildDir/docs/dokka"
    jdkVersion = 8
}

val sourcesJar by tasks.creating(Jar::class) {
    from(sourceSets.get("main").allSource)
    classifier = "sources"
}

val javadocJar by tasks.creating(Jar::class) {
    from(dokka.get().outputDirectory)
    classifier = "javadoc"
}
javadocJar.dependsOn(dokka)

application {
    mainClassName = props.applicationMainClassName
}

// https://github.com/bintray/gradle-bintray-plugin
val publicationName = "jcenterPublications"

publishing {
    publications.create(publicationName, MavenPublication::class) {
        from(components.get("java"))
        artifact(sourcesJar)
        artifact(javadocJar)
        groupId = props.artifactGroup
        artifactId = props.artifactName
        version = props.artifactVersion
        pom.withXml {
            asNode().apply {
                appendNode("name", props.packageName)
                appendNode("description", props.packageDescription)
                appendNode("url", props.packageWebsiteUrl)
                appendNode("licenses")
                        .appendNode("license").apply {
                            appendNode("name", props.packageLicenseName)
                            appendNode("url", props.packageLicenseUrl)
                        }
                appendNode("developers")
                        .appendNode("developer").apply {
                            appendNode("id", props.developerId)
                            appendNode("name", props.developerName)
                            appendNode("email", props.developerEmail)
                        }
                appendNode("scm").appendNode("url", props.packageVcsUrl)
            }
        }
    }
}

bintray {
    user = props.bintrayUsername
    key = props.bintrayApiKey
    setPublications(publicationName)
    dryRun = bintrayDryRun
    publish = bintrayPublish
    override = bintrayOverride
    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = props.packageName
        userOrg = props.bintrayUsername
        websiteUrl = props.packageWebsiteUrl
        issueTrackerUrl = props.packageIssueTrackerUrl
        vcsUrl = props.packageVcsUrl
        setLicenses(props.packageLicenseName)
        setLabels(*props.packageLabels)
        publicDownloadNumbers = true
        version(closureOf<BintrayExtension.VersionConfig> {
            name = props.packageVersionName
            released = props.packageVersionReleased
            vcsTag = props.packageVersionVcsTag
        })
    })
}
