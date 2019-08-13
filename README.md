Redis Cache Module for Play Framework
=====================================

[![Download](https://api.bintray.com/packages/wiill/maven/play-redis/images/download.svg)](https://bintray.com/wiill/maven/play-redis/_latestVersion)

Built over the Jedis connector and Akka serialization for maximum flexibility.
One of the main goal is to provide a consistent implementation over multiple
Play framework versions to allow interoperability.

Features
--------

 - Akka serialization for pluggable serialization protocols.
 - Compression of larger values to reduce latency.
 - 2-tier cache system, recent keys are kept in a local cache.

Usage
-----

Add to your build:

```scala
resolvers += "Archwill Releases" at Resolver.bintrayRepo("wiill", "maven")

libraryDependencies += "net.archwill.play" %% "play-redis" % "1.0.3.play26"
```

Check for the latest version in the download badge above, also use the version
suffix matching your Play's major and minor release. Currently only Play 2.4.x
and 2.5.x are fully supported, the implementation for version 2.6.x and 2.7.x
are incomplete at the moment.

Then enable the module in your configuration, while disabling Ehcache:

```
play.modules.disabled += "play.api.cache.EhCacheModule"
play.modules.enabled += "net.archwill.play.redis.RedisModule"
```

Add the proper configuration to connect to your Redis server, minimally this
should include the host and port to use:

```
redis {
  host = "localhost"
  port = 6379
}
```

Check out the other possible knobs and defaults in
[reference.conf](play-redis/src/main/resources/reference.conf).

### Serialization

Objects will be serialized using Akka Serialization making it possible to use
Kryo or other saner serialization frameworks than Java serialization. Read the
[Akka documentation](https://doc.akka.io/docs/akka/current/serialization.html)
for how to set this up.

### Local Cache

This plugin also makes use of a local cache to speed up lookups for recent keys.
When retrofitting caching in a legacy system making inneficient use of database
requests with duplicated queries per request this can greatly help with response
time.

A Redis Pub-Sub channel is used to communicate the invalidations, which may
introduce a slight delay between when a key is changed and when every
application instance is aware of it. Depending on how your application makes use
of the cache this may be a source of issues.

If needed, disabled it by setting the `redis.local-cache.max-size` configuration
key to 0. You may also want to tweak that value dependeing on the number of
unique keys used and the heap memory available.

Some scenarios where it could be desirable to disable the local cache include:

 - The keys used are vastly different with each request
 - Your application is very sensitive to data freshness
 - Keys are not requested more than once per request
 - Cache keys are changing extremely often

License
-------

[MIT](LICENSE)
