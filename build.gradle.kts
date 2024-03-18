plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.11.0"
}

group = "com.codealike.client.intellij"

version = "1.7.2.0"


repositories {
    mavenCentral()
}

val libs: Configuration by configurations.creating

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {

    version.set("2023.3.3")

    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("233.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(libs.map { if (it.isDirectory) it else zipTree(it) })
    }
}

dependencies {
    libs("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("jakarta.activation:jakarta.activation-api:2.1.0")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.0")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1.MR")
    libs("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpcore:4.4.16")
    implementation("cglib:cglib:3.3.0")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.glassfish.hk2:hk2:3.1.0")
    implementation("org.glassfish.hk2:hk2-utils:3.1.0")
    implementation("org.glassfish.hk2:hk2-locator:3.1.0")
    libs("org.glassfish.jersey.core:jersey-client:3.1.5")
    implementation("org.glassfish.jersey.core:jersey-common:3.1.5")
    implementation("org.glassfish.jersey.inject:jersey-hk2:3.1.5")
    libs("org.osgi:osgi.core:8.0.0")
    implementation("log4j:log4j:1.2.17")
    libs("joda-time:joda-time:2.12.2")
    implementation("nekohtml:nekohtml:1.9.6.2")
    configurations.implementation.get().extendsFrom(libs)
}