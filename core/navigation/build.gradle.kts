plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.forgeflow.core.navigation"
}

dependencies {
    api(libs.androidx.navigation.compose)
    api(libs.kotlin.serialization.core)
}
