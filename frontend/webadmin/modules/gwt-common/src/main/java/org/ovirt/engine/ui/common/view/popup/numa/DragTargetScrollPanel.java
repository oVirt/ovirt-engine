package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.presenter.popup.numa.UpdatedVnumaEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DragTargetScrollPanel extends Composite implements HasHandlers {

    interface WidgetUiBinder extends UiBinder<Widget, DragTargetScrollPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
        String dragOver();
    }

    private final EventBus eventBus;

    @UiField
    FocusPanel container;

    @UiField
    FlowPanel dragTargetPanel;

    @UiField
    Style style;

    private int pNumaNodeIndex;

    @Inject
    public DragTargetScrollPanel(EventBus gwtEventBus) {
        this.eventBus = gwtEventBus;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void clear() {
        dragTargetPanel.clear();
    }

    public void add(IsWidget widget) {
        dragTargetPanel.add(widget);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @UiHandler("container")
    void onPanelDragOver(DragOverEvent event) {
        container.addStyleName(style.dragOver());
    }

    @UiHandler("container")
    void onPanelDragLeave(DragLeaveEvent event) {
        container.removeStyleName(style.dragOver());
    }

    @UiHandler("container")
    void onPanelDragDrop(DropEvent event) {
        String aggregatedString = event.getData("Text"); // $NON-NLS-1$
        String[] dataItems = aggregatedString.split("\\|"); // $NON-NLS-1$
        if (dataItems.length == 3) {
            String vmGid = getValue("VM_GID", dataItems[0]); // $NON-NLS-1$
            if (StringHelper.isNotNullOrEmpty(vmGid)) {
                Guid vmGuid = Guid.createGuidFromString(vmGid);
                boolean pinned = Boolean.valueOf(getValue("PINNED", dataItems[1])); //$NON-NLS-1$
                int vNumaNodeIndex = Integer.parseInt(getValue("INDEX", dataItems[2])); //$NON-NLS-1$
                container.removeStyleName(style.dragOver());
                event.preventDefault();
                UpdatedVnumaEvent.fire(this, vmGuid, pinned, vNumaNodeIndex, pNumaNodeIndex);
            } else {
                container.removeStyleName(style.dragOver());
            }
        }
    }

    private String getValue(String key, String vmGidString) {
        String[] keyValue = vmGidString.split("="); // $NON-NLS-1$
        if (keyValue.length == 2 && keyValue[0].equals(key)) {
            return keyValue[1];
        }
        return "";
    }

    public void setIndex(int numaNodeIndex) {
        this.pNumaNodeIndex = numaNodeIndex;
    }
}
