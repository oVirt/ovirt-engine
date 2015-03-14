package org.ovirt.engine.ui.common.view.popup.numa;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.popup.numa.UnassignedVNumaNodesPanelPresenterWidget;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;

public class UnassignedVNumaNodesPanelView extends AbstractView implements
        UnassignedVNumaNodesPanelPresenterWidget.ViewDef {

    interface ViewUiBinder extends
            UiBinder<Widget, UnassignedVNumaNodesPanelView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Style extends CssResource {
        String scrollPanel();

        String unassignPanel();

        String emptyPanel();
    }

    private static final int UNASSIGNED = -1; // -1 means unassigned NUMA NODE.

    @UiField
    Label unassignedHeaderLabel;

    @UiField
    Label instructionsLabel;

    @UiField
    FlowPanel unassignedNodesPanel;

    @UiField
    FlowPanel nodeDetailPanel;

    @UiField
    Style style;

    @UiField(provided = true)
    final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final Provider<VmTitlePanel> vmTitlePanelProvider;
    private final Provider<DraggableVirtualNumaPanel> virtualNumaPanelProvider;
    private final Provider<DragTargetScrollPanel> scrollPanelProvider;

    /**
     * Constructor.
     */
    @Inject
    public UnassignedVNumaNodesPanelView(
            final Provider<VmTitlePanel> vmTitlePanelProvider,
            final Provider<DraggableVirtualNumaPanel> virtualNumaPanelProvider,
            Provider<DragTargetScrollPanel> scrollPanelProvider) {
        this.vmTitlePanelProvider = vmTitlePanelProvider;
        this.virtualNumaPanelProvider = virtualNumaPanelProvider;
        this.scrollPanelProvider = scrollPanelProvider;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    private void addVNumaNode(IsWidget widget) {
        unassignedNodesPanel.add(widget);
    }

    @Override
    public void addToUnassignedPanel(View view) {
        addVNumaNode(view);
    }

    @Override
    public IsWidget getNodePanelTitle(VM vm, List<VNodeModel> list) {
        VmTitlePanel titlePanel = vmTitlePanelProvider.get();
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscaped(vm.getName());
        titlePanel
                .initWidget(builder.toSafeHtml(), list.size(), vm.getStatus());
        return titlePanel;
    }

    @Override
    public IsWidget getNodePanelContent(VM vm, List<VNodeModel> virtualNodes,
            List<VdsNumaNode> numaNodeList) {
        DragTargetScrollPanel scrollPanel = getScrollPanel();
        scrollPanel.clear();
        for (VNodeModel nodeModel : virtualNodes) {
            DraggableVirtualNumaPanel numaNodePanel = virtualNumaPanelProvider
                    .get();
            numaNodePanel.setModel(nodeModel, numaNodeList);
            scrollPanel.add(numaNodePanel);
        }
        if (virtualNodes.isEmpty()) {
            scrollPanel.addStyleName(style.emptyPanel());
        }
        return scrollPanel;
    }

    @Override
    public void addEmptyUnassignPanel() {
        DragTargetScrollPanel scrollPanel = getScrollPanel();
        scrollPanel.addStyleName(style.unassignPanel());
        addVNumaNode(scrollPanel);
    }

    private DragTargetScrollPanel getScrollPanel() {
        DragTargetScrollPanel scrollPanel = scrollPanelProvider.get();
        scrollPanel.setStyleName(style.scrollPanel());
        scrollPanel.setIndex(UNASSIGNED);
        return scrollPanel;
    }

    @Override
    public void clear() {
        unassignedNodesPanel.clear();
    }
}
