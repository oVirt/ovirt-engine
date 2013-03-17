package org.ovirt.engine.core.searchbackend;

public class AuditCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public AuditCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VM_PLU_OBJ_NAME, SearchObjects.VM_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDS_PLU_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_PLU_OBJ_NAME, SearchObjects.TEMPLATE_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_USER_PLU_OBJ_NAME, SearchObjects.VDC_USER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.QUOTA_OBJ_NAME, SearchObjects.QUOTA_OBJ_NAME);
        buildCompletions();
        mVerbs.put(SearchObjects.VM_OBJ_NAME, SearchObjects.VM_OBJ_NAME);
        mVerbs.put(SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_USER_OBJ_NAME, SearchObjects.VDC_USER_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);
        mVerbs.put(SearchObjects.GLUSTER_VOLUME_OBJ_NAME, SearchObjects.GLUSTER_VOLUME_OBJ_NAME);
    }
}
