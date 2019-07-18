package play.cache;

import java.util.concurrent.Callable;

public interface SyncCacheApi extends CacheApi {

    <T> T getOrElseUpdate(String key, Callable<T> block, int expiration);

    <T> T getOrElseUpdate(String key, Callable<T> block);

    @Override
    default <T> T getOrElse(String key, Callable<T> block, int expiration) {
        return this.getOrElseUpdate(key, block, expiration);
    }

    @Override
    default <T> T getOrElse(String key, Callable<T> block) {
        return this.getOrElseUpdate(key, block);
    }

}
