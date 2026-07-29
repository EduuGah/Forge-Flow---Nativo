plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.workout"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:platform"))
    implementation(libs.androidx.core.ktx)
}
