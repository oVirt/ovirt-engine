package org.ovirt.engine.core.searchbackend;

public class AuditCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public AuditCrossRefAutoCompleter() {
        super(new String[] { SearchObjects.VM_PLU_OBJ_NAME,
                SearchObjects.TEMPLATE_PLU_OBJ_NAME,
                SearchObjects.VDC_USER_PLU_OBJ_NAME,
                SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME,
                SearchObjects.GLUSTER_VOLUME_PLU_OBJ_NAME,
                SearchObjects.QUOTA_OBJ_NAME },
                new String[] {
                SearchObjects.VM_OBJ_NAME, SearchObjects.VDS_OBJ_NAME,
                SearchObjects.TEMPLATE_OBJ_NAME,
                SearchObjects.VDC_USER_OBJ_NAME,
                SearchObjects.VDC_CLUSTER_OBJ_NAME,
                SearchObjects.GLUSTER_VOLUME_OBJ_NAME
        });
    }
}
