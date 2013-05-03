package org.ovirt.engine.core.searchbackend;

public class VdsCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public VdsCrossRefAutoCompleter() {
        super(new String[] {
                SearchObjects.VM_PLU_OBJ_NAME,
                SearchObjects.TEMPLATE_PLU_OBJ_NAME,
                SearchObjects.AUDIT_PLU_OBJ_NAME,
                SearchObjects.VDC_USER_PLU_OBJ_NAME,
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                SearchObjects.VDS_NETWORK_INTERFACE_OBJ_NAME
        }, new String[] {
                SearchObjects.VM_OBJ_NAME,
                SearchObjects.TEMPLATE_OBJ_NAME,
                SearchObjects.AUDIT_OBJ_NAME,
                SearchObjects.VDC_USER_OBJ_NAME
        });
    }
}
