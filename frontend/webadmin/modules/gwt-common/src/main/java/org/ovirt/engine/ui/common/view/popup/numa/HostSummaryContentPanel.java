package org.ovirt.engine.ui.common.view.popup.numa;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class HostSummaryContentPanel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, HostSummaryContentPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FocusPanel numaContainer;

    @UiField
    FlowPanel numaPanel;

    @UiField
    FocusPanel vNumaContainer;

    @UiField
    FlowPanel vNumaPanel;

    @Inject
    Provider<VirtualNumaPanel> virtualNumaPanelProvider;

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public HostSummaryContentPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setModel(List<VdsNumaNode> numaNodes, List<VNodeModel> vNodeModels) {
        for(VNodeModel vNodeModel: vNodeModels) {
            VirtualNumaPanel panel = virtualNumaPanelProvider.get();
            panel.setModel(vNodeModel);
            vNumaPanel.add(panel);
        }
        for(VdsNumaNode numaNode: numaNodes) {
            numaPanel.add(new HTML(messages.numaNode(numaNode.getIndex())));
        }
    }
}
