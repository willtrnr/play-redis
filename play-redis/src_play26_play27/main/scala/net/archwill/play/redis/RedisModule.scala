package net.archwill.play.redis

import javax.inject.Provider

import com.typesafe.config.Config
import play.api.cache.{AsyncCacheApi, SyncCacheApi}
import play.api.inject.Binding
import play.api.{Configuration, Environment}
import play.cache.{AsyncCacheApi => JAsyncCacheApi, SyncCacheApi => JSyncCacheApi}

case class AsyncRedisConfig(
  dispatcher: String
)

class RedisModule extends BaseRedisModule {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    super.bindings(environment, configuration) ++ Seq(
      bind[AsyncRedisConfig].to(new AsyncRedisConfigProvider(configuration.underlying.getConfig("redis"))),
      bind[SyncCacheApi].to[RedisCacheApi],
      bind[JSyncCacheApi].to[JavaRedisCacheApi],
      bind[AsyncCacheApi].to[RedisAsyncCacheApi],
      bind[JAsyncCacheApi].to[JavaRedisAsyncCacheApi]
    )

}

private[redis] class AsyncRedisConfigProvider(config: Config) extends Provider[AsyncRedisConfig] {

  override lazy val get: AsyncRedisConfig = AsyncRedisConfig(
    dispatcher = config.getString("dispatcher")
  )

}
