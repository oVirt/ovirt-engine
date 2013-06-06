package org.ovirt.engine.core.searchbackend;

public class NetworkCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public NetworkCrossRefAutoCompleter() {
        super(SearchObjects.NETWORK_CLUSTER_OBJ_NAME,
                SearchObjects.NETWORK_HOST_OBJ_NAME,
                SearchObjects.PROVIDER_OBJ_NAME);
    }
}
