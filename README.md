# RemoteShardingKt

A discord bot sharder to shard your bot across multiple servers. This project
uses [Netty](https://github.com/netty/netty) and the TCP protocol, so you might have to allow the port you use (5252 by
default) on your firewall.

> NOTE: This project is NOT ready for use in production yet. It's still a "prototype".

## Installation

Make sure you have the [JitPack](https://jitpack.io) maven repository added to your project and then add the dependency.

### Gradle (Groovy)

```groovy
repositories {
  // Existing entries
  maven { url 'https://jitpack.io' } // Maven repository
}

dependencies {
  implementation 'com.github.zihadmahiuddin:remotesharding-kt:master-SNAPSHOT'
}
```

### Gradle (Kotlin)

```kotlin
repositories {
  // Existing entries
  maven("https://jitpack.io") // Maven repository
}

dependencies {
  implementation("com.github.zihadmahiuddin:remotesharding-kt:master-SNAPSHOT")
}
```

### Maven

```xml

<repositories>
  <!-- Existing entries -->
  <repository> <!-- Maven repository -->
    <id>remotesharding-kt</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

```xml

<dependency>
  <groupId>com.github.zihadmahiuddin</groupId>
  <artifactId>remotesharding-kt</artifactId>
  <version>master-SNAPSHOT</version>
</dependency>
```

## Usage

Usage examples are available in the [examples](examples) folder.

You will need to generate a secure encryption key. You can use one of the programs below to generate a key.

### Kotlin

```kotlin
import javax.crypto.KeyGenerator

object Main {
  @JvmStatic
  fun main(args: Array<String>) {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(256)
    val secretKey = keyGen.generateKey()
    println(secretKey.encoded.joinToString("") { "%02x".format(it) })
  }
}
```

### Java

```java
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Main {
  public static void main(String[] args) throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey secretKey = keyGen.generateKey();
    System.out.println(new BigInteger(1, secretKey.getEncoded()).toString(16));
  }
}
```

Feel free to create an issue if you notice something wrong and PRs are always appreciated :)

Thanks.
