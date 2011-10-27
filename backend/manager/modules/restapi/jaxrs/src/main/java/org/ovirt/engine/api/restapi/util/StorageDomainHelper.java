package org.ovirt.engine.api.restapi.util;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;

public class StorageDomainHelper {


    public static storage_server_connections getConnection(StorageType storageType, String address, String target, String userName, String password, Integer port) {
        String portal;
        if (port!=null && port != 0) {
            portal = address + ":" + port;
        } else {
            portal = address;
        }

        return new storage_server_connections(address,
                    null,
                    target,
                    password,
                    storageType,
                    userName,
                    port==null ? null : Integer.toString(port),
                    portal);
    }
}
