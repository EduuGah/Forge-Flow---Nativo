plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.hilt")
}

android {
    namespace = "com.forgeflow.core.platform"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)
}
