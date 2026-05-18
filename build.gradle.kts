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
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

subprojects {
    group = "com.mrc.networklogger"
    version = "0.1.1"
}
