import java.util.Date
import java.text.SimpleDateFormat
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.Closure
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.cli.common.toBooleanLenient

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

val artifactName = project.name
val artifactGroup = project.group.toString()
val artifactVersion = project.version.toString()

val applicationMainClassName = ext["application_main_class_name"] as String

val manifestMainClass = extIfExists("manifest_main_class", applicationMainClassName)
val manifestImplementationTitle = extIfExists("manifest_implementation_title", "$artifactGroup:$artifactName")
val manifestImplementationVersion = extIfExists("manifest_implementation_version", artifactVersion)

val bintrayUsername = extIfExists("bintray_username", System.getenv("BINTRAY_USER") ?: "")
val bintrayApiKey = extIfExists("bintray_api_key", System.getenv("BINTRAY_KEY") ?: "")
val developerId = extIfExists("developer_id", System.getenv("DEVELOPER_ID") ?: "")
val developerName = extIfExists("developer_name", System.getenv("DEVELOPER_NAME") ?: "")
val developerEmail = extIfExists("developer_email", System.getenv("DEVELOPER_EMAIL") ?: "")

val packageName = extIfExists("package_name", "$artifactGroup:$artifactName")
val packageDescription = extIfExists("package_description", "")
val packageWebsiteUrl = extIfExists("package_website_url", "https://github.com/user/repo")
val packageIssueTrackerUrl = extIfExists("package_issue_tracker_url", "$packageWebsiteUrl/issues")
val packageVcsUrl = extIfExists("package_vcs_url", "$packageWebsiteUrl.git")
val packageLicenseName = extIfExists("package_license_name", "MIT")
val packageLicenseUrl = extIfExists("package_license_url", "https://opensource.org/licenses/MIT")
val packageLabels = extIfExists("package_labels", "").split(",").toTypedArray()
val packageVersionName = extIfExists("package_version_name", artifactVersion)
val packageVersionReleased: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(Date())
val packageVersionVcsTag = extIfExists("package_version_vcs_tag", artifactVersion)

val bintrayDryRun = true
val bintrayPublish = true
val bintrayOverride = false


val jar by tasks.existing(Jar::class) {
    manifest(closureOf<Manifest> {
        attributes(mapOf(
                "Main-Class" to manifestMainClass,
                "Implementation-Title" to manifestImplementationTitle,
                "Implementation-Version" to manifestImplementationVersion
        ))
    })
}

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
    mainClassName = applicationMainClassName
}

// https://github.com/bintray/gradle-bintray-plugin
val publicationName = "jcenterPublications"

publishing {
    publications.create(publicationName, MavenPublication::class) {
        from(components.get("java"))
        artifact(sourcesJar)
        artifact(javadocJar)
        groupId = artifactGroup
        artifactId = artifactName
        version = artifactVersion
        pom.withXml {
            asNode().apply {
                appendNode("name", packageName)
                appendNode("description", packageDescription)
                appendNode("url", packageWebsiteUrl)
                appendNode("licenses")
                        .appendNode("license").apply {
                            appendNode("name", packageLicenseName)
                            appendNode("url", packageLicenseUrl)
                        }
                appendNode("developers")
                        .appendNode("developer").apply {
                            appendNode("id", developerId)
                            appendNode("name", developerName)
                            appendNode("email", developerEmail)
                        }
                appendNode("scm").appendNode("url", packageVcsUrl)
            }
        }
    }
}

bintray {
    user = bintrayUsername
    key = bintrayApiKey
    setPublications(publicationName)
    dryRun = bintrayDryRun
    publish = bintrayPublish
    override = bintrayOverride
    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = packageName
        userOrg = bintrayUsername
        websiteUrl = packageWebsiteUrl
        issueTrackerUrl = packageIssueTrackerUrl
        vcsUrl = packageVcsUrl
        setLicenses(packageLicenseName)
        setLabels(*packageLabels)
        publicDownloadNumbers = true
        version(closureOf<BintrayExtension.VersionConfig> {
            name = packageVersionName
            released = packageVersionReleased
            vcsTag = packageVersionVcsTag
        })
    })
}


fun Project.extIfExists(name: String, defaultValue: String): String = if (ext.has(name)) {
    ext[name] as String
} else {
    defaultValue
}
