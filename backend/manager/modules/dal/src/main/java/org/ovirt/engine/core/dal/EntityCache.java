package org.ovirt.engine.core.dal;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.CachedEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ReadDao;

public class EntityCache {

    private static final EntityCache instance = new EntityCache();
    private Map<EntityCacheKey, EntityCacheItem> cache = new HashMap<EntityCacheKey, EntityCacheItem>();

    private EntityCache() {

    }

    public static EntityCache getInstance() {
        return instance;
    }

    public <T extends CachedEntity> T getFromCache(Guid id, ReadDao dao) {
        EntityCacheKey key = new EntityCacheKey(id, dao);
        T instance = EntityCache.getInstance().get(key);
        if (instance == null) {
            instance = (T) dao.get(id);
            EntityCache.getInstance().put(key, instance);
        }
        return instance;

    }

    private <T extends CachedEntity> T get(EntityCacheKey key) {
        EntityCacheItem item = cache.get(key);

        if (item == null)
            return null;

        if (System.currentTimeMillis() - item.getPlacedInCache() > item.getEntity().getExpiration())
            return null;

        return (T) item.getEntity();
    }

    private void put(EntityCacheKey key, CachedEntity entity) {
        cache.put(key, new EntityCacheItem(entity));
    }
    private class EntityCacheItem {
        private CachedEntity entity;
        private long placedInCache;

        public EntityCacheItem(CachedEntity entity) {
            this.entity = entity;
            placedInCache = System.currentTimeMillis();
        }

        public CachedEntity getEntity() {
            return entity;
        }

        public long getPlacedInCache() {
            return placedInCache;
        }
    }

    private class EntityCacheKey {
        private Guid key;
        private ReadDao dao;

        public EntityCacheKey(Guid key, ReadDao dao) {
            super();
            this.key = key;
            this.dao = dao;
        }

        public Guid getKey() {
            return key;
        }

        public void setKey(Guid key) {
            this.key = key;
        }

        public ReadDao getDao() {
            return dao;
        }

        public void setDao(ReadDao dao) {
            this.dao = dao;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((dao == null) ? 0 : dao.getClass().hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EntityCacheKey other = (EntityCacheKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (dao == null) {
                if (other.dao != null)
                    return false;
            } else if (!dao.getClass().equals(other.dao.getClass()))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }

        private EntityCache getOuterType() {
            return EntityCache.this;
        }

    }
}
