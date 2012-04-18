package org.ovirt.engine.core.searchbackend;

public class DiskImageCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public DiskImageCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_PLU_OBJ_NAME);
        buildCompletions();
    }
}
