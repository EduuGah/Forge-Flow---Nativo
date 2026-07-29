plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.settings"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
}
