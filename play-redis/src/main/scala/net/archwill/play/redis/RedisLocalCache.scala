package net.archwill.play.redis

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}

import com.google.common.cache.{Cache, CacheBuilder}
import play.api.Logger
import redis.clients.jedis.{Jedis, JedisPubSub}
import resource._
import scala.util.control.NonFatal

@Singleton
private[redis] class RedisLocalCache @Inject() (config: RedisConfig) {

  private[this] val logger: Logger = Logger(classOf[RedisLocalCache])

  private[this] val channel: String = "::inv"

  private[this] val cache: Cache[String, Array[Byte]] = {
    val b = CacheBuilder.newBuilder().maximumSize(config.localCache.maxSize.toLong)
    config.localCache.expiration.foreach(e => b.expireAfterWrite(e.toMillis, TimeUnit.MILLISECONDS))
    b.build()
  }

  private[this] val invalidator: Thread =
    new Thread("redis-local-cache-invalidator-" + RedisLocalCache.invalidatorId.incrementAndGet) {

      override def run(): Unit = while (!isInterrupted) {
        logger.info(s"Connecting local cache invalidator to Redis at ${config.host}:${config.port}")
        try {
          managed(new Jedis(config.host, config.port, config.timeout.toMillis.toInt)).acquireAndGet { client =>
            config.password.foreach(client.auth)
            client.subscribe(
              new JedisPubSub() {
                override def onMessage(channel: String, message: String): Unit = {
                  if (message == "") {
                    logger.info("Invalidating all keys in local cache")
                    cache.invalidateAll()
                  } else {
                    logger.trace(s"Invalidating key: $message")
                    cache.invalidate(message)
                  }
                }
              },
              channel
            )
          }
        } catch {
          case NonFatal(e) =>
            logger.warn("Local cache invalidator was disconnected from PubSub channel", e)
            Thread.sleep(2000)
        }
      }

    }

  def get(key: String): Option[Array[Byte]] =
    Option(cache.getIfPresent(key))

  def getOrElse(key: String)(orElse: => Option[Array[Byte]]): Option[Array[Byte]] =
    get(key) orElse {
      val v = orElse
      v.foreach(cache.put(key, _))
      v
    }

  def remove(key: String)(implicit client: Jedis): Unit = {
    client.publish(channel, key)
    cache.invalidate(key)
  }

  def invalidate()(implicit client: Jedis): Unit = {
    client.publish(channel, "")
    cache.invalidateAll()
  }

  invalidator.start()

}

private[redis] object RedisLocalCache {

  val invalidatorId: AtomicInteger = new AtomicInteger(0)

}
