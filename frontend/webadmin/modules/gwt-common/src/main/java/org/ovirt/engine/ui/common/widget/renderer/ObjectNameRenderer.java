package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class ObjectNameRenderer extends AbstractRenderer<Object[]> {

    @Override
    public String render(Object[] arg) {

        VdcObjectType vdcObjectType = (VdcObjectType) arg[0];
        String objectType = "(" + new EnumRenderer<VdcObjectType>().render(vdcObjectType) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        String objectName = (String) arg[1];
        if (arg.length == 4 && AsyncDataProvider.getInstance().getEntityGuid(arg[2]).equals(arg[3])) {
            return ""; //$NON-NLS-1$
        }
        if (vdcObjectType.equals(VdcObjectType.System)) {
            return objectType;
        }
        return objectName + " " + objectType; //$NON-NLS-1$
    }
}
