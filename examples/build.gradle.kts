plugins {
  id("com.github.johnrengelman.shadow") version "6.1.0"
  java
  kotlin("jvm")
}

group = "dev.zihad.remotesharding"

val jarName = "remotesharding-examples"

val jdaVersion = "4.3.0_295"
val logbackVersion = "1.2.3"

repositories {
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
}

dependencies {
  implementation(kotlin("stdlib"))

  implementation(project(":lib"))

  implementation("ch.qos.logback:logback-classic:$logbackVersion")

  implementation("net.dv8tion:JDA:$jdaVersion")
}

tasks {
  shadowJar {
    mergeServiceFiles()
  }

  val shadowJarClient = register<Jar>("shadowJarClient") {
    dependsOn(shadowJar)
    archiveBaseName.set("${jarName}-client")
    manifest {
      attributes(mapOf("Main-Class" to "dev.zihad.remotesharding.examples.Client"))
    }
    from(zipTree((shadowJar.get().archiveFile)))
  }
  val shadowJarJavaClient = register<Jar>("shadowJarJavaClient") {
    dependsOn(shadowJar)
    archiveBaseName.set("${jarName}-javaclient")
    manifest {
      attributes(mapOf("Main-Class" to "dev.zihad.remotesharding.examples.JavaClient"))
    }
    from(zipTree((shadowJar.get().archiveFile)))
  }

  val shadowJarServer = register<Jar>("shadowJarServer") {
    dependsOn(shadowJar)

    archiveBaseName.set("$jarName-server")
    manifest {
      attributes(mapOf("Main-Class" to "dev.zihad.remotesharding.examples.Server"))
    }
    from(zipTree((shadowJar.get().archiveFile)))
  }
  val shadowJarJavaServer = register<Jar>("shadowJarJavaServer") {
    dependsOn(shadowJar)

    archiveBaseName.set("$jarName-javaserver")
    manifest {
      attributes(mapOf("Main-Class" to "dev.zihad.remotesharding.examples.JavaServer"))
    }
    from(zipTree((shadowJar.get().archiveFile)))
  }

  build {
    dependsOn(shadowJarClient)
    dependsOn(shadowJarJavaClient)
    dependsOn(shadowJarServer)
    dependsOn(shadowJarJavaServer)
  }

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
