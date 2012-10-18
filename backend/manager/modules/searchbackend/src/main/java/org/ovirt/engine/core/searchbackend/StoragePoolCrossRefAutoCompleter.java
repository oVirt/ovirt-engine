package org.ovirt.engine.core.searchbackend;

public class StoragePoolCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public StoragePoolCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
        mVerbs.put(SearchObjects.NETWORK_OBJ_NAME, SearchObjects.NETWORK_OBJ_NAME);
        buildCompletions();
        mVerbs.put(SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);
    }
}
