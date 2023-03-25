plugins {
    id("java-library")
    id("maven-publish")
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("signing")
}

repositories {
    mavenLocal()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/groups/public/") }
    maven { url = uri("https://repo.dmulloy2.net/content/groups/public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.viaversion.com") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
}

group = "me.catcoder"
version = "6.1.3-SNAPSHOT"
description = "Powerful feature-packed Minecraft scoreboard library"

extra["sonatypeUsername"] = System.getenv("SONATYPE_USERNAME")
extra["sonatypePassword"] = System.getenv("SONATYPE_PASSWORD")

val adventureVersion = "4.13.0"
val paperVersion = "1.19.4-R0.1-SNAPSHOT"
val protocolLibVersion = "4.8.0"
val viaVersionVersion = "4.6.2"
val miniPlaceholdersVersion = "2.0.0"
val nettyVersion = "4.1.67.Final"
val lombokVersion = "1.18.26"

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.powermock:powermock-module-junit4:2.0.7")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")

    compileOnly("io.papermc.paper:paper-api:${paperVersion}")
    testCompileOnly("io.papermc.paper:paper-api:${paperVersion}")

    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    compileOnly("com.viaversion:viaversion-bukkit:${viaVersionVersion}")
    compileOnly("com.comphenix.protocol:ProtocolLib:${protocolLibVersion}")

    compileOnly("io.netty:netty-buffer:4.1.90.Final")

    compileOnly("io.github.miniplaceholders:miniplaceholders-api:${miniPlaceholdersVersion}")

    compileOnly("net.kyori:adventure-api:${adventureVersion}")
    compileOnly("net.kyori:adventure-text-minimessage:${adventureVersion}")
    compileOnly("net.kyori:adventure-text-serializer-gson:${adventureVersion}")
    compileOnly("net.kyori:adventure-text-serializer-legacy:${adventureVersion}")
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
}

nexusPublishing {
    repositories {
        sonatype()
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
