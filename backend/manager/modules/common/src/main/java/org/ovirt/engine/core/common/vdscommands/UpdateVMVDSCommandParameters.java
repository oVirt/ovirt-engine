package org.ovirt.engine.core.common.vdscommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;

public class UpdateVMVDSCommandParameters extends StorageDomainIdParametersBase {
    public UpdateVMVDSCommandParameters(Guid storagePoolId,
            Map<Guid, KeyValuePairCompat<String, List<Guid>>> infoDictionary) {
        super(storagePoolId);
        setInfoDictionary((infoDictionary != null) ? infoDictionary
                : new HashMap<Guid, KeyValuePairCompat<String, List<Guid>>>());
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("infoDictionary.size", getInfoDictionary().size());
    }
}
