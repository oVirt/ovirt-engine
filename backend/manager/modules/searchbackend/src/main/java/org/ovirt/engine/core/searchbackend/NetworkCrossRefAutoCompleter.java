package org.ovirt.engine.core.searchbackend;

public class NetworkCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public NetworkCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.NETWORK_CLUSTER_OBJ_NAME, SearchObjects.NETWORK_CLUSTER_OBJ_NAME);
        buildCompletions();
    }
}
