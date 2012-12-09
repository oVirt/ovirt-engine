package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class UpdateVMVDSCommandParameters extends StorageDomainIdParametersBase {
    public UpdateVMVDSCommandParameters(Guid storagePoolId,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> infoDictionary) {
        super(storagePoolId);
        setInfoDictionary((infoDictionary != null) ? infoDictionary
                : new java.util.HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>());
    }

    private Map<Guid, KeyValuePairCompat<String, List<Guid>>> privateInfoDictionary;

    public Map<Guid, KeyValuePairCompat<String, List<Guid>>> getInfoDictionary() {
        return privateInfoDictionary;
    }

    private void setInfoDictionary(Map<Guid, KeyValuePairCompat<String, List<Guid>>> value) {
        privateInfoDictionary = value;
    }

    public UpdateVMVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, infoDictionary.size = %s", super.toString(), getInfoDictionary().size());
    }
}
