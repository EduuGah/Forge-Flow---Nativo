import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.configureAndroid(extension: ApplicationExtension) {
    configureAndroidCommon(extension)
    extension.apply {
        defaultConfig {
            minSdk = 26
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        configureSharedOptions()
    }
}

internal fun Project.configureAndroid(extension: LibraryExtension) {
    configureAndroidCommon(extension)
    extension.apply {
        defaultConfig {
            minSdk = 26
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        configureSharedOptions()
    }
}

private fun Project.configureAndroidCommon(extension: CommonExtension) {
    extension.apply {
        compileSdk = 36
        compileSdkMinor = 1
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

private fun ApplicationExtension.configureSharedOptions() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

private fun LibraryExtension.configureSharedOptions() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

internal fun Project.addLibrary(configuration: String, alias: String) {
    dependencies.add(configuration, libs.findLibrary(alias).get())
}

internal fun Project.addPlatform(configuration: String, alias: String) {
    dependencies.add(configuration, dependencies.platform(libs.findLibrary(alias).get()))
}
