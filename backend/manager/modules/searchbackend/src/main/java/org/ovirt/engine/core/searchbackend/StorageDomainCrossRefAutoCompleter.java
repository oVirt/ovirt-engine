package org.ovirt.engine.core.searchbackend;

public class StorageDomainCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public StorageDomainCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VDS_PLU_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME);
        buildCompletions();
        mVerbs.put(SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);
    }
}
