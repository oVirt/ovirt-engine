package org.ovirt.engine.api.restapi.util;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;

public class StorageDomainHelper {


    public static storage_server_connections getConnection(StorageType storageType, String address, String target, String userName, String password, Integer port) {
        return new storage_server_connections(address,
                    null,
                    target,
                    password,
                    storageType,
                    userName,
                    port==null ? null : Integer.toString(port),
                    "0");//TODO: when VSDM and Backend will support this, we will need to externalize portal to the user
    }
}
