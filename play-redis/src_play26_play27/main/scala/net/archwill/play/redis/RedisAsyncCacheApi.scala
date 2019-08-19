package net.archwill.play.redis

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, blocking}
import scala.reflect.ClassTag

import javax.inject.{Inject, Singleton}

import akka.Done
import akka.actor.ActorSystem
import play.api.cache.AsyncCacheApi

@Singleton
class RedisAsyncCacheApi @Inject() (cache: RedisCacheApi, config: AsyncRedisConfig, system: ActorSystem) extends AsyncCacheApi {

  implicit private[this] val dispatcher: ExecutionContext =
    system.dispatchers.lookup(config.dispatcher)

  override def get[T: ClassTag](key: String): Future[Option[T]] =
    Future(blocking { cache.get(key) })

  override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => Future[A]): Future[A] =
    get(key) flatMap {
      case Some(o) =>
        Future.successful(o)
      case None =>
        val f = orElse
        f.foreach(cache.set(key, _, expiration))
        f
    }

  override def set(key: String, value: Any, expiration: Duration): Future[Done] =
    Future(blocking {
      cache.set(key, value, expiration)
      Done
    })

  override def remove(key: String): Future[Done] =
    Future(blocking {
      cache.remove(key)
      Done
    })

  def removeAll(): Future[Done] =
    Future(blocking {
      cache.invalidate()
      Done
    })

  override lazy val sync: RedisCacheApi = cache

}
