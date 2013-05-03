package org.ovirt.engine.core.searchbackend;

public class DiskCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public DiskCrossRefAutoCompleter() {
        super(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
    }
}
