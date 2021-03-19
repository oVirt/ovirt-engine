package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ExternalDataStatus {

    private Map<VmExternalDataKind, Integer> retrievalAttempts;
    private List<VmExternalDataKind> finishedKinds;

    public ExternalDataStatus() {
        retrievalAttempts = new EnumMap<>(VmExternalDataKind.class);
        finishedKinds = new ArrayList<>(2);
    }

    public Integer incFailedRetrievalAttempts(VmExternalDataKind dataKind) {
        retrievalAttempts.put(dataKind,
                retrievalAttempts.getOrDefault(dataKind, Integer.valueOf(0)) + 1);
        return retrievalAttempts.get(dataKind);
    }

    public boolean getFinished(VmExternalDataKind dataKind) {
        return finishedKinds.contains(dataKind);
    }

    public void setFinished(VmExternalDataKind dataKind) {
        finishedKinds.add(dataKind);
    }
}
