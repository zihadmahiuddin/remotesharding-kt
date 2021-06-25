plugins {
  `java-library`
  kotlin("jvm")
  kotlin("plugin.serialization") version "1.5.0"
  `maven-publish`
}

val projectGroupId = "dev.zihad"
val projectArtifactId = "remotesharding"
val projectVersion = "1.0.2"

val jdaVersion = "4.3.0_295"
val kotlinxCoroutinesVersion = "1.5.0"
val kotlinxSerializationVersion = "1.2.1"
val ktorVersion = "1.6.0"
val nettyVersion = "4.1.65.Final"
val slf4jVersion = "1.7.31"

repositories {
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
}

dependencies {
  implementation(kotlin("reflect"))

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-java:$ktorVersion")
  implementation("io.ktor:ktor-client-serialization:$ktorVersion")

  api("io.netty:netty-all:$nettyVersion")

  api("org.slf4j:slf4j-api:$slf4jVersion")

  api("net.dv8tion:JDA:$jdaVersion")
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }

  compileTestKotlin {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = projectGroupId
      artifactId = projectArtifactId
      version = projectVersion

      from(components["java"])
    }
  }
  repositories {
    maven("file://${layout.projectDirectory.asFile.parentFile.absolutePath}/maven-repository")
  }
}
