plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.compose")
    id("forgeflow.android.testing")
}

android {
    namespace = "com.forgeflow.core.designsystem"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.compose.material.icons.extended)
}
