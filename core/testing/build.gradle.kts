plugins {
    id("forgeflow.android.library")
    id("forgeflow.android.testing")
}

android {
    namespace = "com.forgeflow.core.testing"
}

dependencies {
    api(project(":core:data"))
    api(project(":core:model"))
    api(libs.coroutines.test)
    api(libs.junit)
}
