package net.archwill.play.redis

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

import java.util.Base64
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.serialization.{Serialization, SerializationExtension}
import play.api.cache.SyncCacheApi
import redis.clients.jedis.{Jedis, JedisPool}
import resource._

@Singleton
class RedisCacheApi @Inject() (pool: JedisPool, system: ActorSystem) extends SyncCacheApi {

  import scala.reflect.{ClassTag => Scala}, net.archwill.play.redis.{JavaClassTag => Java}

  private[this] val client: ManagedResource[Jedis] = managed(pool.getResource)

  private[this] val serder: Serialization = SerializationExtension(system)

  override def get[T: ClassTag](key: String): Option[T] =
    Option(client.acquireAndGet(_.get(key))).map(decode(_).asInstanceOf[T])

  override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => A): A =
    get[A](key) getOrElse {
      val v = orElse
      set(key, v, expiration)
      v
    }

  override def set(key: String, value: Any, expiration: Duration): Unit = {
    if (value == null) {
      remove(key)
    } else {
      val encoded = encode(value)
      client acquireAndGet { c =>
        if (expiration.isFinite) {
          c.setex(key, expiration.toSeconds.toInt, encoded)
        } else {
          c.set(key, encoded)
        }
        ()
      }
    }
  }

  override def remove(key: String): Unit =
    client acquireAndGet { c =>
      c.del(key)
      ()
    }

  def invalidate(): Unit =
    client acquireAndGet { c =>
      c.flushDB()
      ()
    }

  private[this] def encode(value: Any): String = value match {
    case null =>
      throw new UnsupportedOperationException("Null is not supported by the redis cache implementation")
    case str: String =>
      str
    case prim if prim.getClass.isPrimitive || Primitives.primitives.contains(prim.getClass) =>
      prim.toString
    case obj: AnyRef =>
      val data = serder.findSerializerFor(obj).toBinary(obj)
      Base64.getEncoder().encodeToString(data)
    case _ =>
      throw new UnsupportedOperationException("Cannot serialize type " + value.getClass)
  }

  private[this] def decode[T](value: String)(implicit tag: ClassTag[T]): Any = tag match {
    case Java.String =>
      value
    case Java.Boolean | Scala.Boolean =>
      value.toBoolean
    case Java.Byte | Scala.Byte =>
      value.toByte
    case Java.Char | Scala.Char =>
      value.charAt(0)
    case Java.Short | Scala.Short =>
      value.toShort
    case Java.Int | Scala.Int =>
      value.toInt
    case Java.Long | Scala.Long =>
      value.toLong
    case Java.Float | Scala.Float =>
      value.toFloat
    case Java.Double | Scala.Double =>
      value.toDouble
    case Scala.Nothing =>
      throw new IllegalArgumentException("Cannot get an instance of Nothing from cache")
    case _ =>
      val data = Base64.getDecoder.decode(value)
      serder.deserialize(data, tag.runtimeClass.asInstanceOf[Class[_ <: AnyRef]]).get
  }

}

private[redis] object Primitives {

  val primitives: Set[Class[_]] = Set(
    classOf[Boolean], classOf[java.lang.Boolean],
    classOf[Byte], classOf[java.lang.Byte],
    classOf[Char], classOf[java.lang.Character],
    classOf[Short], classOf[java.lang.Short],
    classOf[Int], classOf[java.lang.Integer],
    classOf[Long], classOf[java.lang.Long],
    classOf[Float], classOf[java.lang.Float],
    classOf[Double], classOf[java.lang.Double]
  )

}

private[redis] object JavaClassTag {

  val Boolean = ClassTag(classOf[java.lang.Boolean])
  val Byte = ClassTag(classOf[java.lang.Byte])
  val Char = ClassTag(classOf[java.lang.Character])
  val Short = ClassTag(classOf[java.lang.Short])
  val Int = ClassTag(classOf[java.lang.Integer])
  val Long = ClassTag(classOf[java.lang.Long])
  val Float = ClassTag(classOf[java.lang.Float])
  val Double = ClassTag(classOf[java.lang.Double])
  val String = ClassTag(classOf[java.lang.String])

}
