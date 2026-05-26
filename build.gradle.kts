import org.gradle.kotlin.dsl.invoke

/*dependencies{
    configurations.all {
        resolutionStrategy {
            force(platform(libs.androidx.compose.bom))
        }
    }
}*/
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

subprojects {
    val isJitPackBuild = System.getenv("JITPACK") == "true"
    val jitPackGroup = System.getenv("GROUP")
    val jitPackArtifact = System.getenv("ARTIFACT")

    group = if (isJitPackBuild && !jitPackGroup.isNullOrBlank() && !jitPackArtifact.isNullOrBlank()) {
        "$jitPackGroup.$jitPackArtifact"
    } else {
        "com.mrc.networklogger"
    }
    version = if (isJitPackBuild) {
        System.getenv("VERSION") ?: "0.2.0"
    } else {
        "0.2.0"
    }
}
