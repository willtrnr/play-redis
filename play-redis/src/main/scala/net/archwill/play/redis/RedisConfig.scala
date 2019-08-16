package net.archwill.play.redis

import java.time.Duration

import redis.clients.jedis.JedisPoolConfig

case class RedisConfig(
  host: String,
  port: Int,
  timeout: Duration,
  password: Option[String],
  database: Int,
  poolConfig: JedisPoolConfig,
  compressThreshold: Int,
  localCache: RedisLocalCacheConfig
)

case class RedisLocalCacheConfig(
  maxSize: Int,
  expiration: Option[Duration]
)
