plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "custom-names"
include(
    "custom-names-api",
    "custom-names-plugin",
)
