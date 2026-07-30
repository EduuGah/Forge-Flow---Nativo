plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.history"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:platform"))
}
