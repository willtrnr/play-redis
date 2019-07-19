Redis Cache Module for Play Framework
=====================================

Built over the Jedis connector and Akka serialization for maximum flexibility.
One of the main goal is to provide a consistent implementation over multiple
Play framework versions to allow interoperability.

Usage
-----

Add to your build:

```scala
resolvers += "Archwill Releases" at Resolver.bintrayRepo("wiill", "maven")

libraryDependencies += "net.archwill.play" %% "play-redis" % "1.0.0.play26"
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
}
```

Objects will be serialized using Akka Serialization making it possible to use
Kryo or other saner serialization frameworks than Java serialization.
