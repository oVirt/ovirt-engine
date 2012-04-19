package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.vms.IIsObjectInSetup;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to IIsObjectInSetup
 */
public class IsObjectInSystemColumn<T> extends ImageResourceColumn<T> {

    private IIsObjectInSetup inSetup;

    public IIsObjectInSetup getInSetup() {
        return inSetup;
    }

    public void setInSetup(IIsObjectInSetup inSetup) {
        this.inSetup = inSetup;
    }

    @Override
    public ImageResource getValue(T object) {
        if (inSetup.isObjectInSetup(object)) {
            return getCommonResources().logNormalImage();
        }
        return null;
    }

}
