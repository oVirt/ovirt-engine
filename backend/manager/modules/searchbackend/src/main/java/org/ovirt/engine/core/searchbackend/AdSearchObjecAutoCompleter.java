package org.ovirt.engine.core.searchbackend;

public class AdSearchObjecAutoCompleter extends BaseAutoCompleter {
    public AdSearchObjecAutoCompleter() {
        super(new String[] { SearchObjects.AD_USER_PLU_OBJ_NAME, SearchObjects.AD_GROUP_PLU_OBJ_NAME },
                new String[] { SearchObjects.AD_USER_OBJ_NAME, SearchObjects.AD_GROUP_OBJ_NAME });
    }

    @Override
    public String changeCaseDisplay(String text) {
        return text.substring(0, 1).toUpperCase() + (text.substring(0, 0) + text.substring(0 + 1)).toLowerCase();
    }
}
