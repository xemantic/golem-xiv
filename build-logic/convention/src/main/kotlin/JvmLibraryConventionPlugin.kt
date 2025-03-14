//import com.google.samples.apps.nowinandroid.configureKotlinJvm
//import com.google.samples.apps.nowinandroid.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.jvm")
            apply(plugin = "nowinandroid.android.lint")

            //configureKotlinJvm()
            dependencies {
                //"testImplementation"(libs.findLibrary("kotlin.test").get())
            }
        }
    }
}
