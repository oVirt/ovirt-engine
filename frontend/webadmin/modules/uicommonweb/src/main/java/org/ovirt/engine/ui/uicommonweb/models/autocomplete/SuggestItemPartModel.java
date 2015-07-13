package org.ovirt.engine.ui.uicommonweb.models.autocomplete;

import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class SuggestItemPartModel extends Model {

    private SuggestItemPartType privatePartType = SuggestItemPartType.values()[0];

    public SuggestItemPartType getPartType() {
        return privatePartType;
    }

    public void setPartType(SuggestItemPartType value) {
        privatePartType = value;
    }

    private String privatePartString;

    public String getPartString() {
        return privatePartString;
    }

    public void setPartString(String value) {
        privatePartString = value;
    }

}
