package net.archwill.play.redis

import scala.concurrent.Future
import scala.util.control.NonFatal

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Inject, Singleton}

import com.google.common.cache.{Cache, CacheBuilder}
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import redis.clients.jedis.{Jedis, JedisPubSub}
import resource._

@Singleton
private[redis] class RedisLocalCache @Inject() (config: RedisConfig, lifecycle: ApplicationLifecycle) {

  import RedisLocalCache._

  private[this] val logger: Logger = Logger(classOf[RedisLocalCache])

  private[this] val channel: String = "::_cache_inv"

  private[this] val localCache: Cache[String, Array[Byte]] = {
    val b = CacheBuilder.newBuilder().maximumSize(config.localCache.maxSize.toLong)
    config.localCache.expiration.foreach(e => b.expireAfterWrite(e.toMillis, TimeUnit.MILLISECONDS))
    b.build()
  }

  // FIXME: This thing is very messy, but the blocking Jedis PubSub handling there's very few we can do
  private[this] val invalidator: Thread =
    new Thread("redis-local-cache-invalidator-" + threadId.incrementAndGet) {

      @volatile private[this] var current: Jedis = _

      private[this] val handler: JedisPubSub = new JedisPubSub() {
        override def onMessage(channel: String, message: String): Unit = message match {
          case "" =>
            logger.info("Invalidating all keys in local cache")
            localCache.invalidateAll()
          case _ =>
            logger.trace(s"Invalidating key: $message")
            localCache.invalidate(message)
        }
      }

      override def run(): Unit = while (!isInterrupted) {
        logger.info(s"Connecting local cache invalidator to Redis at ${config.host}:${config.port}")
        try {
          managed(new Jedis(config.host, config.port, config.timeout.toMillis.toInt)) acquireAndGet { client =>
            config.password.foreach(client.auth)
            current = client
            client.subscribe(handler, channel)
          }
        } catch {
          case _: InterruptedException =>
            interrupt()
          case NonFatal(e) =>
            if (!isInterrupted) {
              logger.warn("Local cache invalidator was disconnected from PubSub channel, waiting 2s before reconnecting...", e)
              Thread.sleep(2000)
            }
        } finally {
          current = null
        }
      }

      override def start(): Unit = {
        lifecycle.addStopHook { () =>
          interrupt()
          if (current ne null) current.close()
          Future.successful(())
        }
        super.start()
      }

    }

  def get(key: String, compute: => Option[Array[Byte]]): Option[Array[Byte]] =
    Option(localCache.getIfPresent(key)).map(duplicate) orElse {
      val r = compute
      for (v <- r) {
        localCache.put(key, duplicate(v))
      }
      r
    }

  def remove(key: String)(implicit client: Jedis): Unit = {
    client.publish(channel, key)
    localCache.invalidate(key)
  }

  def invalidate()(implicit client: Jedis): Unit = {
    client.publish(channel, "")
    localCache.invalidateAll()
  }

  private[this] def duplicate(src: Array[Byte]): Array[Byte] = {
    val dst = new Array[Byte](src.length)
    System.arraycopy(src, 0, dst, 0, dst.length)
    dst
  }

  invalidator.start()

}

private[redis] object RedisLocalCache {

  val threadId: AtomicInteger = new AtomicInteger(0)

}
