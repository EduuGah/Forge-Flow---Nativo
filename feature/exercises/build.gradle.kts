plugins {
    id("forgeflow.android.feature")
}

android {
    namespace = "com.forgeflow.feature.exercises"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))

    testImplementation(project(":core:testing"))
}
