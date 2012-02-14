package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map_id;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class StorageDomainDAOWrapperImpl extends BaseDAOWrapperImpl implements StorageDomainDAO {
    StorageDomainStaticDAOHibernateImpl staticDAO = new StorageDomainStaticDAOHibernateImpl();
    StorageDomainDynamicDAOHibernateImpl dynamicDAO = new StorageDomainDynamicDAOHibernateImpl();
    StoragePoolIsoMapDAOHibernateImpl storagePoolIsoMapDAO = new StoragePoolIsoMapDAOHibernateImpl();
    ImageGroupStorageDomainMapDAOHibernateImpl imageGroupStorageDomainMapDAO =
            new ImageGroupStorageDomainMapDAOHibernateImpl();

    @Override
    public void setSession(Session session) {
        super.setSession(session);
        staticDAO.setSession(session);
        dynamicDAO.setSession(session);
        storagePoolIsoMapDAO.setSession(session);
        imageGroupStorageDomainMapDAO.setSession(session);
    }

    @Override
    public Guid getMasterStorageDomainIdForPool(Guid pool) {
        Guid returnValue = Guid.Empty;
        List<storage_domains> domains = getAllForStoragePool(pool);
        for (storage_domains domain : domains) {
            if (domain.getstorage_domain_type() == StorageDomainType.Master) {
                returnValue = domain.getId();
                break;
            }
        }
        return returnValue;
    }

    @Override
    public storage_domains get(Guid id) {
        storage_domain_static staticPart = staticDAO.get(id);

        return createDomain(staticPart);
    }

    private storage_domains createDomain(storage_domain_static staticPart) {
        storage_domains result = null;

        if (staticPart != null) {
            @SuppressWarnings("deprecation")
            storage_domain_dynamic dynamicPart = dynamicDAO.get(staticPart.getId());
            if (dynamicPart != null) {
                result = new storage_domains();

                result.setStorageStaticData(staticPart);
                result.setStorageDynamicData(dynamicPart);

                fillInDetails(result);
            }
        }

        return result;
    }

    private void fillInDetails(storage_domains result) {
        Query query;

        query = getSession().createQuery("select spimap from storage_pool_iso_map spimap," +
                "storage_pool pool " +
                "where spimap.id.storagePoolId = pool.id " +
                "and spimap.id.storageId = :id");

        query.setParameter("id", result.getId());

        @SuppressWarnings("unchecked")
        List<storage_pool_iso_map> spimaps = query.list();

        result.setStoragePoolIsoMapData(spimaps.size() > 0 ? spimaps.get(0) : null);
    }

    @Override
    public storage_domains getForStoragePool(Guid id, NGuid storagepool) {
        storage_domain_static staticPart = staticDAO.getForStoragePool(id, storagepool);

        if (staticPart != null) {
            return createDomain(staticPart);
        }

        return null;
    }

    @Override
    public List<storage_domains> getAllForConnection(String connection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<storage_domains> getAllForStoragePool(Guid pool) {
        return createDomains(staticDAO.getAllForStoragePool(pool));
    }

    private List<storage_domains> createDomains(List<storage_domain_static> staticParts) {
        List<storage_domains> result = new ArrayList<storage_domains>();

        for (storage_domain_static staticPart : staticParts) {
            storage_domains domain = createDomain(staticPart);

            if (domain != null)
                result.add(domain);
        }

        return result;
    }

    @Override
    public List<storage_domains> getAllForImageGroup(NGuid imageGroup) {
        return createDomains(staticDAO.getAllForImageGroup(imageGroup));
    }

    @Override
    public List<storage_domains> getAllForStorageDomain(Guid id) {
        return createDomains(staticDAO.getAllForStorageDomain(id));
    }

    @Override
    public List<storage_domains> getAllWithQuery(String query) {
        return createDomains(staticDAO.findAllWithSQL(query));
    }

    @Override
    public List<storage_domains> getAll() {
        return createDomains(staticDAO.getAll());
    }

    @Override
    public List<Guid> getAllStorageDomainsByImageGroup(Guid imageGroupId) {
        List<storage_domain_static> domains = staticDAO.getAllForImageGroup(imageGroupId);
        List<Guid> result = new ArrayList<Guid>();

        for (storage_domain_static domain : domains) {
            result.add(domain.getId());
        }

        return result;
    }

    @Override
    public void remove(Guid id) {
        dynamicDAO.remove(id);
        staticDAO.remove(id);
    }

    @Override
    public image_group_storage_domain_map getImageGroupStorageDomainMapForImageGroupAndStorageDomain(image_group_storage_domain_map map) {
        return imageGroupStorageDomainMapDAO.get(new image_group_storage_domain_map_id(map.getimage_group_id(),
                map.getstorage_domain_id()));
    }

    @Override
    public void addImageGroupStorageDomainMap(image_group_storage_domain_map map) {
        imageGroupStorageDomainMapDAO.save(map);
    }

    @Override
    public void removeImageGroupStorageDomainMap(image_group_storage_domain_map map) {
        imageGroupStorageDomainMapDAO.remove(new image_group_storage_domain_map_id(map.getimage_group_id(),
                map.getstorage_domain_id()));
    }

    @Override
    public List<image_group_storage_domain_map> getAllImageGroupStorageDomainMapsForStorageDomain(Guid storage_domain_id) {
        return imageGroupStorageDomainMapDAO.findAllForStorageDomain(storage_domain_id);
    }

    @Override
    public List<image_group_storage_domain_map> getAllImageGroupStorageDomainMapsForImage(Guid image_group_id) {
        return imageGroupStorageDomainMapDAO.getAllForImage(image_group_id);
    }

    @Override
    public List<storage_domains> getAllByStoragePoolAndConnection(Guid storagePoolId, String connection) {
        return null;
    }
}
