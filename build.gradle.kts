plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "me.catcoder"
version = "6.2.11-SNAPSHOT"
description = "Powerful feature-packed Minecraft scoreboard library"

val adventureVersion = "5.2.0"
val paperVersion = "1.20.4-R0.1-SNAPSHOT"
val viaVersionVersion = "5.11.0"
val viaNBTVersion = "5.3.0"
val miniPlaceholdersVersion = "2.3.0"
val lombokVersion = "1.18.46"
val foliaLibVersion = "0.5.2"

allprojects {
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<JavaCompile> {
        options.release = 21
        options.encoding = "UTF-8"
    }

    repositories {
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
        maven { url = uri("https://repo.dmulloy2.net/content/groups/public/") }
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        maven { url = uri("https://repo.viaversion.com") }
        maven { url = uri("https://repo.maven.apache.org/maven2/") }
        maven { url = uri("https://repo.opencollab.dev/maven-releases/") }
        maven { url =  uri("https://repo.tcoded.com/releases") }
    }
    dependencies {
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.mockito:mockito-core:5.23.0")
        testImplementation("org.powermock:powermock-module-junit4:2.0.9")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.9")

        compileOnly("io.papermc.paper:paper-api:${paperVersion}")
        testCompileOnly("io.papermc.paper:paper-api:${paperVersion}")

        implementation("com.viaversion:nbt:${viaNBTVersion}")
        implementation("com.tcoded:FoliaLib:${foliaLibVersion}")

        compileOnly("org.projectlombok:lombok:${lombokVersion}")
        annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

        compileOnly("com.viaversion:viaversion-common:${viaVersionVersion}")
        compileOnly("com.viaversion:viaversion-bukkit:${viaVersionVersion}")

        compileOnly("io.netty:netty-buffer:4.2.17.Final")
        compileOnly("io.netty:netty-handler:4.2.17.Final")

        compileOnly("io.github.miniplaceholders:miniplaceholders-api:${miniPlaceholdersVersion}")

        compileOnly("net.kyori:adventure-api:${adventureVersion}")
        compileOnly("net.kyori:adventure-text-minimessage:${adventureVersion}")
        compileOnly("net.kyori:adventure-text-serializer-gson:${adventureVersion}")
        compileOnly("net.kyori:adventure-text-serializer-legacy:${adventureVersion}")
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {

    // Configure all publications
    publications {

        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(javadocJar.get())

            // Provide artifacts information requited by Maven Central
            pom {
                name.set("ProtocolSidebar")
                description.set(project.description)
                url.set("https://github.com/CatCoderr/ProtocolSidebar")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("CatCoder")
                        name.set("Ruslan Onischenko")
                        email.set("catcoderr@gmail.com")
                    }
                    developer {
                        id.set("Presti")
                        name.set("Presti")
                        email.set("protocolsidebar@presti.me")
                    }
                }
                scm {
                    url.set("https://github.com/CatCoderr/ProtocolSidebar")
                    connection.set("scm:git:git://github.com:CatCoderr/ProtocolSidebar.git")
                    developerConnection.set("scm:git:ssh://github.com:CatCoderr/ProtocolSidebar.git")
                }

                issueManagement {
                    url.set("https://github.com/CatCoderr/ProtocolSidebar/issues")
                }

            }
        }
    }

    repositories {
        maven {
            name = "badgames-snapshots"
            url = uri("https://repo.badgames.de/snapshots")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_SECRET_KEY")
    val signingPassword = System.getenv("GPG_PASSPHRASE")

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
