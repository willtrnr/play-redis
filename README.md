Redis Cache Module for Play Framework
=====================================

[![Download](https://api.bintray.com/packages/wiill/maven/play-redis/images/download.svg)](https://bintray.com/wiill/maven/play-redis/_latestVersion)

Built over the Jedis connector and Akka serialization for maximum flexibility.
One of the main goal is to provide a consistent implementation over multiple
Play framework versions to allow interoperability.

Features
--------

 - Akka serialization for pluggable serialization protocols.
 - Compression of larger values to reduce bandwidth.
 - 2-tier cache system, recent keys are kept in a local cache.

Usage
-----

Add to your build:

```scala
resolvers += "Archwill Releases" at Resolver.bintrayRepo("wiill", "maven")

libraryDependencies += "net.archwill.play" %% "play-redis" % "1.0.1.play26"
```

Use the version matching your Play's major and minor release. Currently Play
2.4.x and 2.5.x are supported, the implementation for version 2.6.x and 2.7.x
are incomplete at the moment.

Then enable the module in your configuration, while disabling Ehcache:

```
play.modules.disabled += "play.api.cache.EhCacheModule"
play.modules.enabled += "net.archwill.play.redis.RedisModule"
```

You must also add the proper configuration to connect to your Redis server, the
defaults are:

```
redis {

  host = "localhost"
  port = 6379
  timeout = 10s
  password = null
  database = 1

  # For Play 2.6+ only, the Akka dispatcher to use for the async cache interface
  dispatcher = akka.actor.default-dispatcher

  pool {
    min-idle = 2
    max-idle = 8
    max-total = 8
  }

  local-cache {
    max-size = 1000
    expiration = null
  }
}
```

Objects will be serialized using Akka Serialization making it possible to use
Kryo or other saner serialization frameworks than Java serialization.
