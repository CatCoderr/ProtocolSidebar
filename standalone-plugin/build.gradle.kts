import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":"))
}

tasks.withType<ShadowJar> {
    archiveFileName.set("ProtocolSidebar-${rootProject.version}.jar")
    relocate("com.tcoded.folialib", "me.catcoder.protocolsidebar.lib.folialib")

    // create final jar in project root dir
    destinationDirectory.set(rootProject.rootDir.resolve("bin"))
}

tasks {
    withType<ProcessResources> {

        val tokens = mapOf(
            "projectDescription" to rootProject.description,
            "projectVersion" to rootProject.version
        )

        filesMatching("*.yml") {
            expand(tokens)
        }
    }
}