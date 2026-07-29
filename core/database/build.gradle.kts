plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.hilt")
    id("forgeflow.android.room")
    id("forgeflow.android.testing")
}

android {
    namespace = "com.forgeflow.core.database"
}

dependencies {
    implementation(project(":core:model"))
}
