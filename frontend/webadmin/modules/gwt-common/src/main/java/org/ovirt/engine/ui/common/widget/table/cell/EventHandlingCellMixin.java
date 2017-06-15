package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.view.client.CellPreviewEvent;

public class EventHandlingCellMixin {

    public static boolean inputHandlesClick(CellPreviewEvent<EntityModel> event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (!BrowserEvents.CLICK.equals(nativeEvent.getType())) {
            return false;
        }
        Element target = nativeEvent.getEventTarget().cast();
        return "input".equals(target.getTagName().toLowerCase()); //$NON-NLS-1$
    }

    public static boolean selectOptionHandlesClick(CellPreviewEvent<EntityModel> event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (!BrowserEvents.CLICK.equals(nativeEvent.getType())) {
            return false;
        }
        Element target = nativeEvent.getEventTarget().cast();
        String tagName = target.getTagName().toLowerCase();
        return "select".equals(tagName) || "option".equals(tagName); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
