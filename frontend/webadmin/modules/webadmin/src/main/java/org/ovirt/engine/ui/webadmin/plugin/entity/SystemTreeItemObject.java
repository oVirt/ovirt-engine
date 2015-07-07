package org.ovirt.engine.ui.webadmin.plugin.entity;

import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsMutableObjectWithProperties;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay type representing a system tree item passed through plugin API as native JS object.
 */
public final class SystemTreeItemObject extends JsMutableObjectWithProperties {

    protected SystemTreeItemObject() {
    }

    public static SystemTreeItemObject from(SystemTreeItemModel model) {
        SystemTreeItemObject obj = JavaScriptObject.createObject().cast();

        // TODO(vszocs) currently using SystemTreeItemType enum name
        obj.setValueAsString("type", model.getType().name()); //$NON-NLS-1$

        Object entity = model.getEntity();
        if (entity != null) {
            obj.setValueAsJavaScriptObject("entity", EntityObject.from(entity)); //$NON-NLS-1$
        }

        return obj;
    }

}
