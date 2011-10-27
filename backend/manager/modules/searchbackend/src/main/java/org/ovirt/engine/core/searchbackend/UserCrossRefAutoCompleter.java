package org.ovirt.engine.core.searchbackend;

public class UserCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public UserCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VM_PLU_OBJ_NAME, SearchObjects.VM_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDS_PLU_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_PLU_OBJ_NAME, SearchObjects.TEMPLATE_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.AUDIT_PLU_OBJ_NAME, SearchObjects.AUDIT_PLU_OBJ_NAME);
        buildCompletions();
        mVerbs.put(SearchObjects.VM_OBJ_NAME, SearchObjects.VM_OBJ_NAME);
        mVerbs.put(SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME);
        mVerbs.put(SearchObjects.AUDIT_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME);
    }
}
