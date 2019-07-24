package net.archwill.play.redis

import java.time.Duration
import javax.inject.Provider

import com.typesafe.config.Config
import play.api.cache.CacheApi
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import play.cache.{CacheApi => JCacheApi}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}

case class RedisConfig(
  host: String,
  port: Int,
  timeout: Duration,
  password: Option[String],
  database: Int,
  poolConfig: JedisPoolConfig,
  localCache: RedisLocalCacheConfig
)

case class RedisLocalCacheConfig(
  maxSize: Int,
  expiration: Option[Duration]
)

class RedisModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[RedisConfig].to(new RedisConfigProvider(configuration.underlying.getConfig("redis"))),
    bind[RedisLocalCache].toSelf,
    bind[JedisPool].toProvider[JedisPoolProvider],
    bind[CacheApi].to[RedisCacheApi],
    bind[JCacheApi].to[JavaRedisCacheApi]
  )

}

private[redis] class RedisConfigProvider(config: Config) extends Provider[RedisConfig] {

  override lazy val get: RedisConfig = RedisConfig(
    host = config.getString("host"),
    port = config.getInt("port"),
    timeout = config.getDuration("timeout"),
    password = if (!config.getIsNull("password")) Some(config.getString("password")) else None,
    database = config.getInt("database"),
    poolConfig = {
      val c = new JedisPoolConfig
      c.setMinIdle(config.getInt("pool.min-idle"))
      c.setMaxIdle(config.getInt("pool.max-idle"))
      c.setMaxTotal(config.getInt("pool.max-total"))
      c
    },
    localCache = RedisLocalCacheConfig(
      maxSize = config.getInt("local-cache.max-size"),
      expiration = {
        if (!config.getIsNull("local-cache.expiration"))
          Some(config.getDuration("local-cache.expiration"))
        else
          None
      }
    )
  )

}
