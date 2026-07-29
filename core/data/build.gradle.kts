plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.hilt")
    id("forgeflow.android.testing")
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

    androidTestImplementation(libs.androidx.room.testing)
}
