package org.ovirt.engine.ui.common.view;

import com.gwtplatform.mvp.client.view.CenterPopupPositioner;

public class OvirtCenterPopupPositioner extends CenterPopupPositioner {

    public OvirtCenterPopupPositioner() {
        super();
    }

    @Override
    protected int getLeft(int popupWidth) {
        int left = super.getLeft(popupWidth);
        return left < 0 ? 0 : left;
    }

    @Override
    protected int getTop(int popupHeight) {
        int top = super.getTop(popupHeight);
        return top < 0 ? 0 : top;
    }
}
