package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.ui.uicommonweb.ViewFilter;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public enum NetworkHostFilter implements ViewFilter<NetworkHostFilter>{
    attached(ConstantsManager.getInstance().getConstants().attachedHost()),
    unattached(ConstantsManager.getInstance().getConstants().unattachedHost());

    private String text;

    NetworkHostFilter(String text){
        this.text = text;
    }

    @Override
    public String getText(){
        return text;
    }

    @Override
    public NetworkHostFilter getValue() {
        return this;
    }

}
