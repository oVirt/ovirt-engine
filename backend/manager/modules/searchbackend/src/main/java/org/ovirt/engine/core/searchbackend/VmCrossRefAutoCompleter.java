package org.ovirt.engine.core.searchbackend;

public class VmCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public VmCrossRefAutoCompleter() {
        super(new String[] {
                SearchObjects.VDS_PLU_OBJ_NAME,
                SearchObjects.TEMPLATE_PLU_OBJ_NAME,
                SearchObjects.AUDIT_PLU_OBJ_NAME,
                SearchObjects.VDC_USER_PLU_OBJ_NAME,
                SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME,
                SearchObjects.VM_NETWORK_INTERFACE_OBJ_NAME
        },
                new String[] {
                        SearchObjects.VDS_OBJ_NAME,
                        SearchObjects.TEMPLATE_OBJ_NAME,
                        SearchObjects.AUDIT_OBJ_NAME,
                        SearchObjects.VDC_USER_OBJ_NAME
                });
    }
}
