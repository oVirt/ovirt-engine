package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.webadmin.widget.editor.AnimatedVerticalPanel;
import org.ovirt.engine.ui.webadmin.widget.form.DnDPanel;

import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.Widget;

public class UnassignedNetworksPanel extends DnDPanel{

    private final AnimatedVerticalPanel animatedPanel = new AnimatedVerticalPanel();
    private NetworkPanelsStyle style;
    private HostSetupNetworksModel setupModel;

    public UnassignedNetworksPanel() {
        super(false);

        // drag enter
        addBitlessDomHandler(new DragEnterHandler() {
            @Override
            public void onDragEnter(DragEnterEvent event) {
                doDrag(event, false);
            }
        }, DragEnterEvent.getType());

        // drag over
        addBitlessDomHandler(new DragOverHandler() {

            @Override
            public void onDragOver(DragOverEvent event) {
                doDrag(event, false);
            }
        }, DragOverEvent.getType());

        // drag leave
        addBitlessDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                animatedPanel.getElement().removeClassName(style.networkGroupDragOver());
            }
        }, DragLeaveEvent.getType());

        // drop
        addBitlessDomHandler(new DropHandler() {

            @Override
            public void onDrop(DropEvent event) {
                doDrag(event, true);
                animatedPanel.getElement().removeClassName(style.networkGroupDragOver());
            }
        }, DropEvent.getType());

        setWidget(animatedPanel);
    }

    public void setStyle(final NetworkPanelsStyle style){
        this.style = style;
        animatedPanel.getElement().addClassName(style.unassignedNetworksPanel());
    }

    public void addAll(Collection<? extends Widget> list, boolean fadeIn) {
        animatedPanel.addAll(list, fadeIn);
    }

    @Override
    public void clear() {
        animatedPanel.clear();
    }

    public void setSpacing(int spacing) {
        animatedPanel.setSpacing(spacing);
    }

    private void doDrag(DragDropEventBase<?> event, boolean isDrop) {
        String data = event.getData(NetworkItemPanel.SETUP_NETWORKS_DATA);
        if (data != null) {
            if (setupModel.candidateOperation(data, null, isDrop)) {
               animatedPanel.getElement().addClassName(style.networkGroupDragOver());
                // allow drag/drop (look at http://www.w3.org/TR/html5/dnd.html#dndevents)
                event.preventDefault();
            }
        }
    }

    public void setSetupModel(HostSetupNetworksModel setupModel){
        this.setupModel = setupModel;
    }
}
