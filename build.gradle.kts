plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "me.catcoder"
version = "6.2.10-SNAPSHOT"
description = "Powerful feature-packed Minecraft scoreboard library"

val adventureVersion = "4.16.0"
val paperVersion = "1.20.1-R0.1-SNAPSHOT"
val viaVersionVersion = "5.0.0"
val viaNBTVersion = "5.1.2"
val miniPlaceholdersVersion = "2.2.3"
val lombokVersion = "1.18.30"
val foliaLibVersion = "main-SNAPSHOT"

allprojects {
    apply(plugin = "java-library")

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
    }
    dependencies {
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.mockito:mockito-core:5.7.0")
        testImplementation("org.powermock:powermock-module-junit4:2.0.9")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.9")

        compileOnly("io.papermc.paper:paper-api:${paperVersion}")
        testCompileOnly("io.papermc.paper:paper-api:${paperVersion}")

        implementation("com.viaversion:nbt:${viaNBTVersion}")
        implementation("com.github.technicallycoded:FoliaLib:${foliaLibVersion}")

        compileOnly("org.projectlombok:lombok:${lombokVersion}")
        annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

        compileOnly("com.viaversion:viaversion-common:${viaVersionVersion}")
        compileOnly("com.viaversion:viaversion-bukkit:${viaVersionVersion}")

        compileOnly("io.netty:netty-buffer:4.1.101.Final")
        compileOnly("io.netty:netty-handler:4.1.101.Final")

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
            name = "Snapshots"
            url = uri("https://catcoder.pl.ua/snapshots")
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

    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications["mavenJava"])
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
