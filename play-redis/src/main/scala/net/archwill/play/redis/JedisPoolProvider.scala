package net.archwill.play.redis

import scala.concurrent.Future

import javax.inject.{Inject, Provider, Singleton}

import play.api.Logger
import play.api.inject.ApplicationLifecycle
import redis.clients.jedis.JedisPool
import resource._

@Singleton
private[redis] class JedisPoolProvider @Inject() (config: RedisConfig, lifecycle: ApplicationLifecycle) extends Provider[JedisPool] {

  private[this] val logger = Logger(classOf[JedisPoolProvider])

  override lazy val get: JedisPool = {
    logger.info(s"Connecting to redis at ${config.host}:${config.port}?database=${config.database}")
    val pool = new JedisPool(
      config.poolConfig,
      config.host,
      config.port,
      config.timeout.toMillis.toInt,
      config.password.orNull,
      config.database
    )
    // Make sure our pool can handle requests before signing off on it
    managed(pool.getResource).acquireAndGet(_.ping())
    lifecycle.addStopHook(() => Future.successful(pool.close()))
    pool
  }

}
