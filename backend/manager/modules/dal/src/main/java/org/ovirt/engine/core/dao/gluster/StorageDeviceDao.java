package org.ovirt.engine.core.dao.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsDao;
import org.ovirt.engine.core.dao.ModificationDao;

/**
 * Interface for DB operations on Storage Device Entities
 */
public interface StorageDeviceDao extends ModificationDao<StorageDevice, Guid>, MassOperationsDao<StorageDevice, Guid>, Serializable {

    StorageDevice get(Guid id);

    List<StorageDevice> getStorageDevicesInHost(Guid hostId);

    void updateIsFreeFlag(Guid deviceId, boolean isFree);

 }
