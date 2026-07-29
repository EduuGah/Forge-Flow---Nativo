plugins {
    id("forgeflow.android.application")
    id("forgeflow.android.compose")
    id("forgeflow.android.hilt")
    id("forgeflow.android.testing")
}

android {
    namespace = "com.forgeflow.app"

    defaultConfig {
        applicationId = "com.forgeflow.app"
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:exercises"))
    implementation(project(":feature:history"))
    implementation(project(":feature:home"))
    implementation(project(":feature:routines"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:workout"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
}
