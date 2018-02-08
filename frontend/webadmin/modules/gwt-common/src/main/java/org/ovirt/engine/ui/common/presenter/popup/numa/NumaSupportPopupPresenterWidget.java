package org.ovirt.engine.ui.common.presenter.popup.numa;

import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.CollapsiblePanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;

public class NumaSupportPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<NumaSupportModel, NumaSupportPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<NumaSupportModel> {
        public void setUnassignedGroupPanel(View view);

        public void setHostSummaryPanel(IsWidget widget);

        public IsWidget getHostSummaryTitle(int totalCpus, int usedCpus, int totalMemory, int usedMemory,
                int totalNumaNodes, int totalVNumaNodes);

        public void addVNumaInfoPanel(Set<VdsNumaNode> numaNodes, int nodeIndex, NumaSupportModel supportModel);

        public IsWidget getHostSummaryContent(VDS selectedItem, NumaSupportModel supportModel);

        public void displayVmDetails(VNodeModel vNodeModel);

        void clear();
    }

    public static final NestedSlot TYPE_SetUnassignedPanel = new NestedSlot();

    private final UnassignedVNumaNodesPanelPresenterWidget unassignedVNumaNodesPanelPresenterWidget;

    private final Provider<CollapsiblePanelPresenterWidget> collapsiblePanelProvider;

    private NumaSupportModel supportModel;

    public static final Object TYPE_revealHostSummary = new Object();

    public static final Object TYPE_revealSocketInfo = new Object();

    @Inject
    public NumaSupportPopupPresenterWidget(EventBus eventBus, ViewDef view,
            UnassignedVNumaNodesPanelPresenterWidget unassignedVNumaNodesPanelPresenterWidget,
            Provider<CollapsiblePanelPresenterWidget> collapsiblePanelProvider) {
        super(eventBus, view);
        this.collapsiblePanelProvider = collapsiblePanelProvider;
        this.unassignedVNumaNodesPanelPresenterWidget = unassignedVNumaNodesPanelPresenterWidget;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(final NumaSupportModel model) {
        super.init(model);
        supportModel = model;
        this.unassignedVNumaNodesPanelPresenterWidget.setModel(model);
        supportModel.getModelReady().addListener((ev, sender, args) -> modelReady());
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(UpdatedVnumaEvent.getType(),
                event -> {
                    if (event.getTargetNumaNodeIndex() != -1) {
                        supportModel.pinVNode(event.getSourceVmGuid(), event.getSourceVNumaNodeIndex(),
                                event.getTargetNumaNodeIndex());
                    } else {
                        supportModel.unpinVNode(event.getSourceVmGuid(), event.getSourceVNumaNodeIndex());
                    }
                }));

        registerHandler(getEventBus().addHandler(NumaVmSelectedEvent.getType(),
                event -> getView().displayVmDetails(event.getVNodeModel())));
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_SetUnassignedPanel, unassignedVNumaNodesPanelPresenterWidget);
        getView().setUnassignedGroupPanel(unassignedVNumaNodesPanelPresenterWidget.getView());
    }

    private void modelReady() {
        unassignedVNumaNodesPanelPresenterWidget.populateView();
        getView().clear();
        populateHostSummary();
        populateSockets();
    }

    private void populateSockets() {
        for (int i = 0; i < supportModel.getFirstLevelDistanceSetList().size(); i++) {
            getView().addVNumaInfoPanel(supportModel.getFirstLevelDistanceSetList().get(i).getSecond(), i, supportModel);
        }
    }

    private void populateHostSummary() {
        CollapsiblePanelPresenterWidget hostSummaryPanel = collapsiblePanelProvider.get();
        setInSlot(TYPE_revealHostSummary, hostSummaryPanel);
        List<VM> vmsWithVNuma = supportModel.getVmsWithvNumaNodeList();
        int totalVNuma = 0;
        for (VM vm: vmsWithVNuma) {
            totalVNuma += vm.getvNumaNodeList().size();
        }

        VDS selectedVds = supportModel.getHosts().getSelectedItem();

        hostSummaryPanel.getView().setTitleWidget(getView().getHostSummaryTitle(
                selectedVds.getCpuThreads(),
                selectedVds.getUsageCpuPercent(),
                selectedVds.getPhysicalMemMb(),
                selectedVds.getMemFree().intValue(),
                supportModel.getNumaNodeList().size(), totalVNuma));
        hostSummaryPanel.getView().addContentWidget(getView().getHostSummaryContent(
                selectedVds, supportModel));
        getView().setHostSummaryPanel(hostSummaryPanel);
    }
}
