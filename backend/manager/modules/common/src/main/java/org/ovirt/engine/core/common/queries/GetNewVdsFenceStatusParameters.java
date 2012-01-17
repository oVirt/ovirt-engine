package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetNewVdsFenceStatusParameters")
public class GetNewVdsFenceStatusParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3663389765505476776L;

    public GetNewVdsFenceStatusParameters() {
        _storagePoolId = Guid.Empty;
    }

    public GetNewVdsFenceStatusParameters(Guid vds_id, Guid storagePolId, String managementIp,
            ValueObjectMap fencinOptions, String pmType, String user, String password) {
        _vds_id = vds_id;
        _storagePoolId = storagePolId;
        _managementIp = managementIp;
        _fencingOptions = fencinOptions;
        _pmType = pmType;
        _user = user;
        _password = password;
    }

    @XmlElement(name = "VdsId", nillable = true)
    private Guid _vds_id;

    public Guid getVdsId() {
        return _vds_id;
    }

    public void setVdsId(Guid value) {
        _vds_id = value;
    }

    @XmlElement(name = "StoragePoolId")
    private Guid _storagePoolId = new Guid();

    public Guid getStoragePoolId() {
        return _storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        _storagePoolId = value;
    }

    @XmlElement(name = "ManagementIp")
    private String _managementIp;

    public String getManagementIp() {
        return _managementIp;
    }

    public void setManagementIp(String value) {
        _managementIp = value;
    }

    @XmlElement(name = "FencingOptions", nillable = true)
    private ValueObjectMap _fencingOptions;

    public ValueObjectMap getFencingOptions() {
        return _fencingOptions;
    }

    public void setFencingOptions(ValueObjectMap value) {
        _fencingOptions = value;
    }

    @XmlElement(name = "PmType")
    private String _pmType;

    public String getPmType() {
        return _pmType;
    }

    public void setPmType(String value) {
        _pmType = value;
    }

    @XmlElement(name = "User")
    private String _user;

    public String getUser() {
        return _user;
    }

    public void setUser(String value) {
        _user = value;
    }

    @XmlElement(name = "Password")
    private String _password;

    public String getPassword() {
        return _password;
    }

    public void setPassword(String value) {
        _password = value;
    }

    @XmlElement(name = "IsNewHost")
    private boolean _isNewHost;

    public boolean getIsNewHost() {
        return _isNewHost;
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.IQUERYABLE;
    }

}
