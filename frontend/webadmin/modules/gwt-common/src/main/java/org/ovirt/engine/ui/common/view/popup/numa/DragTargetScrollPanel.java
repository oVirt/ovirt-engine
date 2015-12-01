package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
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
        Guid vmGuid = Guid.createGuidFromString(event.getData("VM_GID")); //$NON-NLS-1$
        boolean pinned = Boolean.valueOf(event.getData("PINNED")); //$NON-NLS-1$
        int vNumaNodeIndex = Integer.parseInt(event.getData("INDEX")); //$NON-NLS-1$
        container.removeStyleName(style.dragOver());
        event.preventDefault();
        UpdatedVnumaEvent.fire(this, vmGuid, pinned, vNumaNodeIndex, pNumaNodeIndex);
    }

    private Pair<Guid, Pair<Boolean, Integer>> parseDropString(String dropString) {
        Pair<Boolean, Integer> pinnedIndexPair = new Pair<>();
        String[] splitString = dropString.split("_"); //$NON-NLS-1$
        pinnedIndexPair.setFirst(Boolean.valueOf(splitString[1]));
        pinnedIndexPair.setSecond(Integer.valueOf(splitString[2]));
        return new Pair<>(Guid.createGuidFromString(splitString[0]), pinnedIndexPair);
    }

    public void setIndex(int numaNodeIndex) {
        this.pNumaNodeIndex = numaNodeIndex;
    }
}
