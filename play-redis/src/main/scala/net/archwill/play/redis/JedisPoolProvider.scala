package net.archwill.play.redis

import javax.inject.{Inject, Provider, Singleton}

import play.api.Logger
import redis.clients.jedis.{JedisPool, JedisPoolConfig}

@Singleton
private[redis] class JedisPoolProvider @Inject() (config: RedisConfig) extends Provider[JedisPool] {

  private[this] val logger = Logger(classOf[JedisPoolProvider])

  override lazy val get: JedisPool = {
    logger.info(s"Connecting to redis at ${config.host}:${config.port}?database=${config.database}")
    new JedisPool(
      new JedisPoolConfig,
      config.host,
      config.port,
      config.timeout.toMillis.toInt,
      config.password.orNull,
      config.database
    )
  }

}
