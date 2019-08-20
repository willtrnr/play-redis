package net.archwill.play.redis

import javax.inject.Provider

import com.typesafe.config.Config
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import redis.clients.jedis.{JedisPool, JedisPoolConfig}

private[redis] abstract class BaseRedisModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[RedisConfig].to(new RedisConfigProvider(configuration.underlying.getConfig("redis"))),
    bind[JedisPool].toProvider[JedisPoolProvider],
    bind[RedisLocalCache].toSelf
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
    compressThreshold = config.getBytes("compress-threshold").toInt,
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
