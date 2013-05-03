package org.ovirt.engine.core.searchbackend;

public class UserCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public UserCrossRefAutoCompleter() {
        super(new String[] {
                SearchObjects.VM_PLU_OBJ_NAME,
                SearchObjects.VDS_PLU_OBJ_NAME,
                SearchObjects.TEMPLATE_PLU_OBJ_NAME,
                SearchObjects.AUDIT_PLU_OBJ_NAME
        },
                new String[] {
                        SearchObjects.VM_OBJ_NAME,
                        SearchObjects.VDS_OBJ_NAME,
                        SearchObjects.TEMPLATE_OBJ_NAME,
                        SearchObjects.AUDIT_OBJ_NAME
                });
    }
}
