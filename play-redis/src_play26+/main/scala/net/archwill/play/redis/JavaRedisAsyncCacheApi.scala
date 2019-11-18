package net.archwill.play.redis

import java.util.Optional
import java.util.concurrent.{Callable, CompletableFuture, CompletionStage, Executor}
import java.util.function.{Consumer => JConsumer, Function => JFunction, Supplier => JSupplier}
import javax.inject.{Inject, Singleton}

import akka.Done
import akka.actor.ActorSystem
import play.cache.{AsyncCacheApi => JAsyncCacheApi}

@Singleton
class JavaRedisAsyncCacheApi @Inject() (cache: JavaRedisCacheApi, config: AsyncRedisConfig, system: ActorSystem) extends JAsyncCacheApi {

  implicit private[this] val dispatcher: Executor =
    system.dispatchers.lookup(config.dispatcher)

  override def get[T](key: String): CompletionStage[T] =
    async(cache.get(key))

  // NOTE: Introduced in Play 2.7.x, because of the shared source we can't use `override`
  def getOptional[T](key: String): CompletionStage[Optional[T]] =
    async(cache.getOptional(key))

  override def getOrElseUpdate[T](key: String, block: Callable[CompletionStage[T]]): CompletionStage[T] =
    getOrElseUpdate(key, block, 0)

  override def getOrElseUpdate[T](key: String, block: Callable[CompletionStage[T]], expiration: Int): CompletionStage[T] =
    getOptional(key).thenComposeAsync(
      new JFunction[Optional[T], CompletionStage[T]] {
        override def apply(o: Optional[T]): CompletionStage[T] =
          if (o.isPresent) {
            CompletableFuture.completedFuture(o.get)
          } else {
            val f = block.call()
            f.thenAcceptAsync(
              new JConsumer[T] {
                override def accept(o: T): Unit =
                  cache.set(key, o, expiration)
              },
              dispatcher
            )
            f
          }
      },
      dispatcher
    )

  override def remove(key: String): CompletionStage[Done] = async {
    cache.remove(key)
    Done
  }

  override def removeAll(): CompletionStage[Done] = async {
    cache.invalidate()
    Done
  }

  override def set(key: String, value: AnyRef): CompletionStage[Done] = async {
    cache.set(key, value)
    Done
  }

  override def set(key: String, value: AnyRef, expiration: Int): CompletionStage[Done] = async {
    cache.set(key, value, expiration)
    Done
  }

  override def sync: JavaRedisCacheApi = cache

  private[this] def async[T](fn: => T): CompletableFuture[T] =
    CompletableFuture.supplyAsync(
      new JSupplier[T]() { override def get(): T = fn },
      dispatcher
    )

}
