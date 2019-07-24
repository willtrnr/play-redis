package net.archwill.play.redis

import scala.concurrent.duration._
import scala.reflect.ClassTag

import java.util.concurrent.Callable
import java.util.{Optional => JOptional}
import javax.inject.{Inject, Singleton}

import play.api.Environment
import play.cache.{SyncCacheApi => JSyncCacheApi}

@Singleton
class JavaRedisCacheApi @Inject() (cache: RedisCacheApi, env: Environment) extends JSyncCacheApi {

  private[this] val tagPrefix: String = "ct::"

  override def get[T](key: String): T =
    getOptional(key).orElse(null.asInstanceOf[T])

  // NOTE: Introduced in Play 2.7.x, because of the shared source we can't use `override`
  def getOptional[T](key: String): JOptional[T] =
    cache.get[String](tagPrefix + key)
      .flatMap { cls => cache.get[T](key)(ClassTag(env.classLoader.loadClass(cls))) }
      .fold(JOptional.empty[T])(JOptional.ofNullable)

  override def getOrElseUpdate[T](key: String, block: Callable[T]): T =
    getOrElseUpdate(key, block, 0)

  override def getOrElseUpdate[T](key: String, block: Callable[T], expiration: Int): T =
    Option(get[T](key)) getOrElse {
      val v = block.call
      set(key, v, expiration)
      v
    }

  override def set(key: String, value: Any): Unit =
    set(key, value, 0)

  override def set(key: String, value: Any, expiration: Int): Unit = {
    if (value == null) {
      remove(key)
    } else {
      val exp = if (expiration <= 0) Duration.Inf else expiration.seconds
      cache.set(key, value, exp)
      cache.set(tagPrefix + key, value.getClass.getCanonicalName, exp)
    }
  }

  override def remove(key: String): Unit = {
    cache.remove(tagPrefix + key)
    cache.remove(key)
  }

  def invalidate(): Unit =
    cache.invalidate()

}
