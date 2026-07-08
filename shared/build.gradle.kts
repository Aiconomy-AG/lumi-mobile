import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)

    }

}


val configuredApiBaseUrl = localProperties.getProperty(
    "API_BASE_URL",
    "https://lumi-server.internship.aico.dev/api"
)

val generatedApiConfigDir = layout.buildDirectory.dir("generated/apiConfig/commonMain/kotlin")

abstract class GenerateApiConfigTask : DefaultTask() {
    @get:Input
    abstract val apiBaseUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val escapedApiBaseUrl = apiBaseUrl.get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

        val outputFile = outputDir
            .file("org/example/project/data/ApiConfig.kt")
            .get()
            .asFile

        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
            package org.example.project.data

            object ApiConfig {
                const val BASE_URL = "$escapedApiBaseUrl"
            }
            """.trimIndent()
        )
    }
}

val generateApiConfig by tasks.registering(GenerateApiConfigTask::class) {
    apiBaseUrl.set(configuredApiBaseUrl)
    outputDir.set(generatedApiConfigDir)
}

tasks.configureEach {
    if (
        name == "compileAndroidMain" ||
        name.startsWith("compile") && name.contains("Kotlin")
    ) {
        dependsOn(generateApiConfig)
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "org.example.project.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain {
            kotlin.srcDir(generatedApiConfigDir)

            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinxJson)
                implementation(libs.ktor.client.logging)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
