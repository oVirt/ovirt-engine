package org.ovirt.engine.ui.common.presenter.popup.numa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.ui.common.presenter.CollapsiblePanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class UnassignedVNumaNodesPanelPresenterWidget extends
    PresenterWidget<UnassignedVNumaNodesPanelPresenterWidget.ViewDef> {

    public interface ViewDef extends View {
        public void addToUnassignedPanel(View view);

        public IsWidget getNodePanelTitle(VM vm, List<VNodeModel> list);

        public IsWidget getNodePanelContent(VM vm, List<VNodeModel> list, List<VdsNumaNode> numaNodeList);

        public void addEmptyUnassignPanel();

        void clear();
    }

    public static final Object TYPE_RevealUnassignedPanels = new Object();

    private final Provider<CollapsiblePanelPresenterWidget> collapisblePanelProvider;

    private NumaSupportModel model;

    @Inject
    public UnassignedVNumaNodesPanelPresenterWidget(EventBus eventBus, ViewDef view,
            Provider<CollapsiblePanelPresenterWidget> collapsiblePanelProvider) {
        super(eventBus, view);
        this.collapisblePanelProvider = collapsiblePanelProvider;
    }

    public void setModel(NumaSupportModel model) {
        this.model = model;
    }

    public void populateView() {
        getView().clear();
        Collection<VNodeModel> unassignedNodeList = model.getUnassignedNumaNodes();
        Map<VM, List<VNodeModel>> vmToNodeMap = new HashMap<>();
        for(VM currentVM: this.model.getVmsWithvNumaNodeList()) {
            List<VNodeModel> nodeModelList = new ArrayList<>();
            vmToNodeMap.put(currentVM, nodeModelList);
        }
        for (VNodeModel vNodeModel: unassignedNodeList) {
            VM currentVM = vNodeModel.getVm();
            List<VNodeModel> nodeModels = vmToNodeMap.get(currentVM);
            nodeModels.add(vNodeModel);
        }
        for(Map.Entry<VM, List<VNodeModel>> entry: vmToNodeMap.entrySet()) {
            CollapsiblePanelPresenterWidget nodePanelPresenter = collapisblePanelProvider.get();
            addToSlot(TYPE_RevealUnassignedPanels, nodePanelPresenter);
            nodePanelPresenter.getView().setTitleWidget(getView().getNodePanelTitle(entry.getKey(), entry.getValue()));
            nodePanelPresenter.getView().addContentWidget(getView().getNodePanelContent(entry.getKey(),
                    entry.getValue(), model.getNumaNodeList()));
            if (entry.getValue().isEmpty()) {
                nodePanelPresenter.collapsePanel();
            }
            getView().addToUnassignedPanel(nodePanelPresenter.getView());
        }
        getView().addEmptyUnassignPanel();
    }

}
