package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.List;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.webadmin.widget.editor.AnimatedVerticalPanel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public abstract class UnassignedNetworksPanel<T extends NetworkItemPanel<?>> extends FocusPanel {

    protected final AnimatedVerticalPanel animatedPanel = new AnimatedVerticalPanel();

    protected NetworkPanelsStyle style;
    private HostSetupNetworksModel setupModel;

    public UnassignedNetworksPanel() {
        getElement().setDraggable(Element.DRAGGABLE_FALSE);

        // drag enter
        addBitlessDomHandler(event -> doDrag(event, false), DragEnterEvent.getType());

        // drag over
        addBitlessDomHandler(event -> doDrag(event, false), DragOverEvent.getType());

        // drag leave
        addBitlessDomHandler(event -> animatedPanel.getElement().removeClassName(style.networkGroupDragOver()), DragLeaveEvent.getType());

        // drop
        addBitlessDomHandler(event -> {
            event.preventDefault();
            doDrag(event, true);
            animatedPanel.getElement().removeClassName(style.networkGroupDragOver());
        }, DropEvent.getType());

        setWidget(animatedPanel);
    }

    public void setStyle(final NetworkPanelsStyle style) {
        this.style = style;
        addStyleName("ts10"); //$NON-NLS-1$
        animatedPanel.getElement().addClassName(style.unassignedNetworksPanel());
    }

    protected void stylePanel(VerticalPanel panel) {
        panel.addStyleName("ts2"); //$NON-NLS-1$
        panel.setWidth("100%"); //$NON-NLS-1$
    }

    public abstract void addAll(List<T> list, boolean fadeIn);

    @Override
    public void clear() {
        animatedPanel.clear();
    }

    public void setSpacing(int spacing) {
        animatedPanel.setSpacing(spacing);
    }

    private void doDrag(DragDropEventBase<?> event, boolean isDrop) {
        String dragDropEventData = NetworkItemPanel.getDragDropEventData(event, isDrop);
        String type = NetworkItemPanel.getType(dragDropEventData);
        String data = NetworkItemPanel.getData(dragDropEventData);
        if (!StringHelper.isNullOrEmpty(data)) {
            if (setupModel.candidateOperation(data, type, null, null, isDrop)) {
                animatedPanel.getElement().addClassName(style.networkGroupDragOver());
                // allow drag/drop (look at http://www.w3.org/TR/html5/dnd.html#dndevents)
                event.preventDefault();
            }
        }
    }

    public void setSetupModel(HostSetupNetworksModel setupModel) {
        this.setupModel = setupModel;
    }

}
