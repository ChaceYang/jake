Jake ID
====
A distributed unique ID generator inspired by [snowflake](https://blog.twitter.com/2010/announcing-snowflake) and [sonyflake](https://github.com/sony/sonyflake)

The default ModelS composed of

    41 bits for time in units of 1 millisecond, Max of 68 years.
    13 bits for a machine id, Used private ipv4 ipv4 [*.*.0-31.0-255][0.0.11111.11111111]
    9  bits for a sequence number, 512 sequence per millisecond

The ModelL composed of

    38 bits for time in units of 10 millisecond, max of 86year.
    16 bits for a machine id, default is private ipv4 [*.*.0-255.0-255][0.0.11111111.11111111]
    9  bits for a sequence number, max of 512 sequence per 10 millisecond

Usage
-----
Maven
```xml
<dependency>
  <groupId>io.github.funcfoo</groupId>
  <artifactId>jake-id-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
Gradle
```groovy
  implementation 'io.github.funcfoo:jake-id-spring-boot-starter:1.0.0-SNAPSHOT'
```

License
-------

The MIT License (MIT)

See [LICENSE](https://github.com/funcfoo/jake/blob/master/LICENSE) for details.
