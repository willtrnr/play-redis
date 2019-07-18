package play.api.cache

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

trait SyncCacheApi extends CacheApi {

  def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => A): A

  override final def getOrElse[A: ClassTag](key: String, expiration: Duration)(orElse: => A): A =
    getOrElseUpdate(key, expiration)(orElse)

}
