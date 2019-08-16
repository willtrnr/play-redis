package net.archwill.play.redis

import play.api.cache.CacheApi
import play.api.inject.Binding
import play.api.{Configuration, Environment}
import play.cache.{CacheApi => JCacheApi}

class RedisModule extends BaseRedisModule {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    super.bindings(environment, configuration) ++ Seq(
      bind[CacheApi].to[RedisCacheApi],
      bind[JCacheApi].to[JavaRedisCacheApi]
    )

}
