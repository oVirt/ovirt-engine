package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.compat.Guid;

public interface IscsiBondDao extends GenericDao<IscsiBond, Guid> {

    public List<IscsiBond> getAllByStoragePoolId(Guid storagePoolId);

    public List<Guid> getNetworkIdsByIscsiBondId(Guid iscsiBondId);

    public List<IscsiBond> getIscsiBondsByNetworkId(Guid netowrkId);

    public void addNetworkToIscsiBond(Guid iscsiBondId, Guid networkId);

    public void removeNetworkFromIscsiBond(Guid iscsiBondId, Guid networkId);

    public List<String> getStorageConnectionIdsByIscsiBondId(Guid iscsiBondId);

    public void addStorageConnectionToIscsiBond(Guid iscsiBondId, String storageConnectionId);

    public void removeStorageConnectionFromIscsiBond(Guid iscsiBondId, String storageConnectionId);
}
