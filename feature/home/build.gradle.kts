plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.home"
}

dependencies {
    implementation(project(":core:data"))
}
