plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.hilt")
    id("forgeflow.android.testing")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.forgeflow.core.data"
}

dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    implementation(project(":core:database"))

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coroutines.core)
    implementation(libs.kotlin.serialization.json)

    androidTestImplementation(libs.androidx.room.testing)
}
