plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:platform"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}
