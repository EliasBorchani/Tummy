package ext

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

fun Project.moduleNamespace(): String {
    val path = path.trim(':').replace(':', '.').replace('-', '_')
    return "com.tummy.$path"
}
