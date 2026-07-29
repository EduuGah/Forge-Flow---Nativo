plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.routines"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
}
