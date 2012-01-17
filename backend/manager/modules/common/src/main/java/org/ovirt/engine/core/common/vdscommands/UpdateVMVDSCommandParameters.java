package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.List;
import java.util.HashMap;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UpdateVMVDSCommandParameters")
public class UpdateVMVDSCommandParameters extends StorageDomainIdParametersBase {
    public UpdateVMVDSCommandParameters(Guid storagePoolId,
            java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> infoDictionary) {
        super(storagePoolId);
        setInfoDictionary((infoDictionary != null) ? infoDictionary
                : new java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>());
    }

    @XmlElement
    private HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> privateInfoDictionary;

    public java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> getInfoDictionary() {
        return privateInfoDictionary;
    }

    private void setInfoDictionary(java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>> value) {
        privateInfoDictionary = value;
    }

    public UpdateVMVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, infoDictionary.size = %s", super.toString(), getInfoDictionary().size());
    }
}
