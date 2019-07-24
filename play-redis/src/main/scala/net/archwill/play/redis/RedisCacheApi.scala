package net.archwill.play.redis

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import scala.util.control.NonFatal

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.{Deflater, DeflaterOutputStream, Inflater, InflaterOutputStream}
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.serialization.{Serialization, SerializationExtension}
import play.api.Logger
import play.api.cache.SyncCacheApi
import redis.clients.jedis.{Jedis, JedisPool}
import resource._

@Singleton
class RedisCacheApi @Inject() (pool: JedisPool, local: RedisLocalCache, system: ActorSystem) extends SyncCacheApi {

  import scala.reflect.{ClassTag => Scala}
  import net.archwill.play.redis.{JavaClassTag => Java}

  private[this] val logger: Logger = Logger(classOf[RedisCacheApi])

  private[this] val rawMarker: Byte = 0x00
  private[this] val deflateMarker: Byte = 0x01

  private[this] val compressThreshold: Int = 1200

  private[this] val serde: Serialization = SerializationExtension(system)

  private[this] val client: ManagedResource[Jedis] = managed(pool.getResource)

  override def get[T: ClassTag](key: String): Option[T] =
    try {
      local.get(key, doGet(key))
        .map(decode(_).asInstanceOf[T])
    } catch {
      case NonFatal(e) =>
        logger.warn(s"Could not get key from cache: $key", e)
        None
    }

  private[this] def doGet(key: String): Option[Array[Byte]] =
    client.acquireAndGet(c => Option(c.get(encodeString(key))))
      .map(decompress)

  override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => A): A =
    get[A](key) getOrElse {
      val v = orElse
      set(key, v, expiration)
      v
    }

  override def set(key: String, value: Any, expiration: Duration): Unit =
    try {
      if (value == null) {
        remove(key)
      } else {
        doSet(key, encode(value), expiration)
      }
    } catch {
      case NonFatal(e) =>
        logger.warn(s"Could not set key [$key] in cache", e)
    }

  def doSet(key: String, value: Array[Byte], exp: Duration): Unit =
    client acquireAndGet { implicit c =>
      if (exp.isFinite) {
        c.setex(encodeString(key), exp.toSeconds.toInt, compress(value))
      } else {
        c.set(encodeString(key), compress(value))
      }
      local.remove(key)
    }

  override def remove(key: String): Unit =
    client acquireAndGet { implicit c =>
      c.del(key)
      local.remove(key)
    }

  def invalidate(): Unit =
    client acquireAndGet { implicit c =>
      c.flushDB()
      local.invalidate()
    }

  private[this] def encodeString(value: String): Array[Byte] =
    value.getBytes(UTF_8)

  private[this] def decodeString(data: Array[Byte]): String =
    new String(data, UTF_8)

  private[this] def encode(value: Any): Array[Byte] = value match {
    case null =>
      throw new IllegalArgumentException("Cannot serialize null")
    case str: String =>
      encodeString(str)
    case prim if prim.getClass.isPrimitive || Primitives.primitives.contains(prim.getClass) =>
      encodeString(prim.toString)
    case obj: AnyRef =>
      serde.serialize(obj).get
    case _ =>
      throw new IllegalArgumentException("Cannot serialize value of type " + value.getClass)
  }

  private[this] def decode[T](data: Array[Byte])(implicit tag: ClassTag[T]): Any = tag match {
    case Java.String =>
      decodeString(data)
    case Java.Boolean | Scala.Boolean =>
      decodeString(data).toBoolean
    case Java.Byte | Scala.Byte =>
      decodeString(data).toByte
    case Java.Char | Scala.Char =>
      decodeString(data).charAt(0)
    case Java.Short | Scala.Short =>
      decodeString(data).toShort
    case Java.Int | Scala.Int =>
      decodeString(data).toInt
    case Java.Long | Scala.Long =>
      decodeString(data).toLong
    case Java.Float | Scala.Float =>
      decodeString(data).toFloat
    case Java.Double | Scala.Double =>
      decodeString(data).toDouble
    case Scala.Nothing =>
      throw new IllegalArgumentException("Cannot deserialize an instance of Nothing")
    case _ =>
      serde.deserialize(data, tag.runtimeClass.asInstanceOf[Class[_ <: AnyRef]]).get
  }

  private[this] def compress(data: Array[Byte]): Array[Byte] = {
    if (data.length > compressThreshold) {
      val out = new ByteArrayOutputStream()
      out.write(deflateMarker.toInt)
      val zip = new DeflaterOutputStream(out, new Deflater(Deflater.BEST_COMPRESSION, true))
      zip.write(data)
      zip.finish()
      zip.flush()
      out.toByteArray
    } else {
      val res = new Array[Byte](data.length + 1)
      res(0) = rawMarker
      System.arraycopy(data, 0, res, 1, data.length)
      res
    }
  }

  private[this] def decompress(data: Array[Byte]): Array[Byte] = {
    if (data.length > 1 && data(0) == deflateMarker) {
      val out = new ByteArrayOutputStream(data.length)
      val zip = new InflaterOutputStream(out, new Inflater(true))
      zip.write(data, 1, data.length - 1)
      zip.finish()
      zip.flush()
      out.toByteArray
    } else {
      val res = new Array[Byte](data.length - 1)
      System.arraycopy(data, 1, res, 0, res.length)
      res
    }
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
