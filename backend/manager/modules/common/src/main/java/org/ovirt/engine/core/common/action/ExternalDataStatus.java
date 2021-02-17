package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ExternalDataStatus {

    private Map<String, Integer> retrievalAttempts;
    private List<String> finishedKinds;

    public ExternalDataStatus() {
        retrievalAttempts = new TreeMap<>();
        finishedKinds = new ArrayList<>(2);
    }

    public Integer incFailedRetrievalAttempts(String dataKind) {
        retrievalAttempts.put(dataKind,
                retrievalAttempts.getOrDefault(dataKind, Integer.valueOf(0)) + 1);
        return retrievalAttempts.get(dataKind);
    }

    public boolean getFinished(String dataKind) {
        return finishedKinds.contains(dataKind);
    }

    public void setFinished(String dataKind) {
        finishedKinds.add(dataKind);
    }
}
