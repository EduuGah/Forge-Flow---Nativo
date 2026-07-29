import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("forgeflow.android.library")
        pluginManager.apply("forgeflow.android.compose")
        pluginManager.apply("forgeflow.android.hilt")
        pluginManager.apply("forgeflow.android.testing")

        dependencies.add("implementation", project(":core:designsystem"))
        dependencies.add("implementation", project(":core:navigation"))
        addLibrary("implementation", "androidx-compose-material-icons-extended")
        addLibrary("implementation", "androidx-hilt-lifecycle-viewmodel-compose")
        addLibrary("implementation", "androidx-lifecycle-runtime-compose")
        addLibrary("implementation", "androidx-lifecycle-viewmodel-compose")
        addLibrary("implementation", "androidx-navigation-compose")
    }
}
