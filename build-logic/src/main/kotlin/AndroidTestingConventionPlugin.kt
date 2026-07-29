import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidTestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        addLibrary("testImplementation", "junit")
        addLibrary("testImplementation", "coroutines-test")
        addLibrary("androidTestImplementation", "androidx-test-core")
        addLibrary("androidTestImplementation", "androidx-test-junit")
        addLibrary("androidTestImplementation", "androidx-test-runner")
        addLibrary("androidTestImplementation", "androidx-espresso-core")
        addLibrary("androidTestImplementation", "coroutines-test")
    }
}
