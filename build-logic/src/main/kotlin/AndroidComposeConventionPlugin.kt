import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> {
                buildFeatures.compose = true
            }
        }
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                buildFeatures.compose = true
            }
        }

        addPlatform("implementation", "androidx-compose-bom")
        addLibrary("implementation", "androidx-compose-foundation")
        addLibrary("implementation", "androidx-compose-material3")
        addLibrary("implementation", "androidx-compose-ui")
        addLibrary("implementation", "androidx-compose-ui-graphics")
        addLibrary("implementation", "androidx-compose-ui-tooling-preview")
        addPlatform("androidTestImplementation", "androidx-compose-bom")
        addLibrary("androidTestImplementation", "androidx-compose-ui-test-junit4")
        addLibrary("debugImplementation", "androidx-compose-ui-test-manifest")
        addLibrary("debugImplementation", "androidx-compose-ui-tooling")
    }
}
