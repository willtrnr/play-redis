package net.archwill.play.redis

import java.time.Duration
import javax.inject.Provider

import com.typesafe.config.Config
import play.api.cache.SyncCacheApi
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import play.cache.{SyncCacheApi => JSyncCacheApi}
import redis.clients.jedis.{JedisPool}

case class RedisConfig(
  host: String,
  port: Int,
  timeout: Duration,
  password: Option[String],
  database: Int,
  dispatcher: String
)

class RedisModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[RedisConfig].to(new RedisConfigProvider(configuration.underlying.getConfig("redis"))),
    bind[JedisPool].toProvider[JedisPoolProvider],
    bind[SyncCacheApi].to[RedisCacheApi],
    bind[JSyncCacheApi].to[JavaRedisCacheApi]
  )

}

private[redis] class RedisConfigProvider(config: Config) extends Provider[RedisConfig] {

  override lazy val get: RedisConfig = RedisConfig(
    host = config.getString("host"),
    port = config.getInt("port"),
    timeout = config.getDuration("timeout"),
    password = if (!config.getIsNull("password")) Some(config.getString("password")) else None,
    database = config.getInt("database"),
    dispatcher = config.getString("dispatcher")
  )

}
