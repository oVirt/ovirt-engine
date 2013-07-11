package org.ovirt.engine.core.common.queries;

import java.util.HashMap;
import org.ovirt.engine.core.common.businessentities.FenceAgentOrder;
import org.ovirt.engine.core.compat.Guid;

public class GetNewVdsFenceStatusParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3663389765505476776L;

    public GetNewVdsFenceStatusParameters() {
        _storagePoolId = Guid.Empty;
    }

    public GetNewVdsFenceStatusParameters(Guid vds_id, Guid vdsGroupId, Guid storagePolId, String managementIp,
            HashMap<String, String> fencinOptions, String pmType, String user, String password, String pmProxyPreferences) {
        _vds_id = vds_id;
        _storagePoolId = storagePolId;
        _managementIp = managementIp;
        _fencingOptions = fencinOptions;
        _pmType = pmType;
        _user = user;
        _password = password;
        this.vdsGrouoId = vdsGroupId;
        this.PmProxyPreferences = pmProxyPreferences;
        this.order = FenceAgentOrder.Primary;
    }

    public GetNewVdsFenceStatusParameters(Guid vds_id, Guid storagePolId, String managementIp,
            HashMap<String, String> fencinOptions, String pmType, String user, String password, FenceAgentOrder order) {
        _vds_id = vds_id;
        _storagePoolId = storagePolId;
        _managementIp = managementIp;
        _fencingOptions = fencinOptions;
        _pmType = pmType;
        _user = user;
        _password = password;
        this.order = order;
    }


    private Guid _vds_id;

    public Guid getVdsId() {
        return _vds_id;
    }

    public void setVdsId(Guid value) {
        _vds_id = value;
    }

    private Guid _storagePoolId = Guid.Empty;

    public Guid getStoragePoolId() {
        return _storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        _storagePoolId = value;
    }

    private String _managementIp;

    public String getManagementIp() {
        return _managementIp;
    }

    public void setManagementIp(String value) {
        _managementIp = value;
    }

    private HashMap<String, String> _fencingOptions;

    public HashMap<String, String> getFencingOptions() {
        return _fencingOptions;
    }

    public void setFencingOptions(HashMap<String, String> value) {
        _fencingOptions = value;
    }

    private String _pmType;

    public String getPmType() {
        return _pmType;
    }

    public void setPmType(String value) {
        _pmType = value;
    }

    private String _user;

    public String getUser() {
        return _user;
    }

    public void setUser(String value) {
        _user = value;
    }

    private String _password;

    public String getPassword() {
        return _password;
    }

    public void setPassword(String value) {
        _password = value;
    }

    private boolean _isNewHost;

    public boolean getIsNewHost() {
        return _isNewHost;
    }

    private Guid vdsGrouoId;

    public Guid getVdsGrouoId() {
        return vdsGrouoId;
    }

    public void setVdsGrouoId(Guid vdsGrouoId) {
        this.vdsGrouoId = vdsGrouoId;
    }

    private String PmProxyPreferences;

    public String getPmProxyPreferences() {
        return PmProxyPreferences;
    }

    public void setPmProxyPreferences(String pmProxyPreferences) {
        PmProxyPreferences = pmProxyPreferences;
    }

    private FenceAgentOrder order;

    public FenceAgentOrder getOrder() {
        return order;
    }

    public void setOrder(FenceAgentOrder order) {
        this.order = order;
    }
}
