package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UnassignedNetworksPanel extends DnDPanel{

    private final ApplicationConstants constants = ClientGinjectorProvider.instance().getApplicationConstants();
    private final AnimatedVerticalPanel animatedPanel = new AnimatedVerticalPanel();

    private final VerticalPanel requiredPanel = new VerticalPanel();
    private final VerticalPanel nonRequiredPanel = new VerticalPanel();
    private final List<VerticalPanel> unassignNetworksList = new ArrayList<VerticalPanel>();
    private final Label requiredLabel = new Label(constants.requiredNetwork());
    private final Label nonRequiredLabel = new Label(constants.nonRequiredNetwork());
    private final SimplePanel requiredTitlePanel = new SimplePanel(requiredLabel);
    private final SimplePanel nonRequiredTitlePanel= new SimplePanel(nonRequiredLabel);

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
                event.preventDefault();
                doDrag(event, true);
                animatedPanel.getElement().removeClassName(style.networkGroupDragOver());
            }
        }, DropEvent.getType());

        unassignNetworksList.add(requiredPanel);
        unassignNetworksList.add(nonRequiredPanel);

        setWidget(animatedPanel);
    }

    public void setStyle(final NetworkPanelsStyle style){
        this.style = style;
        animatedPanel.getElement().addClassName(style.unassignedNetworksPanel());

        // Style required/non-required titles
        requiredTitlePanel.setStyleName(style.requiredTitlePanel());
        nonRequiredTitlePanel.setStyleName(style.requiredTitlePanel());

        requiredLabel.getElement().addClassName(style.requiredLabel());
        nonRequiredLabel.getElement().addClassName(style.requiredLabel());

        // Style required/non-required network list panels
        requiredPanel.setSpacing(2);
        requiredPanel.setWidth("100%"); //$NON-NLS-1$

        nonRequiredPanel.setSpacing(2);
        nonRequiredPanel.setWidth("100%"); //$NON-NLS-1$
    }

    public void addAll(List<NetworkPanel> list, boolean fadeIn) {
        requiredPanel.add(requiredTitlePanel);
        nonRequiredPanel.add(nonRequiredTitlePanel);
        for (NetworkPanel networkPanel : list){
            LogicalNetworkModel networkModel = (LogicalNetworkModel) networkPanel.getItem();
            boolean isRequired = networkModel.getEntity().getCluster() == null ? false : networkModel.getEntity().getCluster().isRequired();
            if (isRequired){
                requiredPanel.add(networkPanel);
            }else{
                nonRequiredPanel.add(networkPanel);
            }
        }
        animatedPanel.addAll(unassignNetworksList, fadeIn);
    }

    @Override
    public void clear() {
        animatedPanel.clear();
        requiredPanel.clear();
        nonRequiredPanel.clear();
    }

    public void setSpacing(int spacing) {
        animatedPanel.setSpacing(spacing);
    }

    private void doDrag(DragDropEventBase<?> event, boolean isDrop) {
        String dragDropEventData = event.getData("Text"); //$NON-NLS-1$
        String type = NetworkItemPanel.getType(dragDropEventData);
        String data = NetworkItemPanel.getData(dragDropEventData);
        if (data != null) {
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
