package org.ovirt.engine.api.restapi.util;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenters;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainHelper {

    public static StorageServerConnections getConnection(StorageType storageType, String address, String target, String userName, String password, Integer port) {
        return new StorageServerConnections(address,
                null,
                target,
                password,
                storageType,
                userName,
                port == null ? null : Integer.toString(port),
                StorageServerConnections.DEFAULT_TPGT);
    }

    /**
     * Adds to the given Storage Domain the references to the Data Centers it is attached to.
     *
     * @param resource the resource that will be used to run the required queries
     * @param model the model of the Storage Domain where the references will be added
     */
    public static void addAttachedDataCenterReferences(BackendResource resource, StorageDomain model) {
        // Note that this implementation is far from efficient, as we are retrieving all the content of the Storage
        // Domains and immediately discarding everything but the identifiers of the Data Centers. It would be better to
        // have a query that returns only the identifiers.
        Guid id = Guid.createGuidFromString(model.getId());
        QueryReturnValue result = resource.runQuery(QueryType.GetStorageDomainListById, new IdQueryParameters(id));
        if (result != null && result.getSucceeded()) {
            List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains = result.getReturnValue();
            if (CollectionUtils.isNotEmpty(storageDomains)) {
                DataCenters dataCenters = new DataCenters();
                for (org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain : storageDomains) {
                    DataCenter dataCenter = new DataCenter();
                    dataCenter.setId(storageDomain.getStoragePoolId().toString());
                    dataCenters.getDataCenters().add(dataCenter);
                }
                model.setDataCenters(dataCenters);
            }
        }
    }
}
