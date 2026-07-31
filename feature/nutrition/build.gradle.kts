plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.nutrition"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
}
