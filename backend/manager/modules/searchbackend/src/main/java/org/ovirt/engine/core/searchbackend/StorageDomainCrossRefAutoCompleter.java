package org.ovirt.engine.core.searchbackend;

public class StorageDomainCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public StorageDomainCrossRefAutoCompleter() {
        super(new String[] {
                    SearchObjects.VDS_PLU_OBJ_NAME,
                    SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME
                },
                new String[] {
                        SearchObjects.VDS_OBJ_NAME,
                        SearchObjects.VDC_CLUSTER_OBJ_NAME
                });
    }
}
