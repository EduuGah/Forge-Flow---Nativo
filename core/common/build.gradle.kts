plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.hilt")
    id("forgeflow.android.testing")
}

android {
    namespace = "com.forgeflow.core.common"
}

dependencies {
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)
}
