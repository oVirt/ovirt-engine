package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractDetailTabListView;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.VmInterfaceActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItemCreator;
import org.ovirt.engine.ui.common.widget.uicommon.vm.VmInterfaceListGroupItem;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineNetworkInterfacePresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class SubTabVirtualMachineNetworkInterfaceView extends
    AbstractDetailTabListView<VM, VmListModel<Void>, VmInterfaceListModel>
        implements SubTabVirtualMachineNetworkInterfacePresenter.ViewDef,
            PatternflyListViewItemCreator<VmNetworkInterface> {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineNetworkInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private PatternflyListView<VM, VmNetworkInterface, VmInterfaceListModel> interfaceListView;
    private final List<ActionButtonDefinition<?, VmNetworkInterface>> actionButtons;


    @Inject
    public SubTabVirtualMachineNetworkInterfaceView(
            SearchableDetailModelProvider<VmNetworkInterface, VmListModel<Void>, VmInterfaceListModel> modelProvider,
            EventBus eventBus, VmInterfaceActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        interfaceListView = new PatternflyListView<>();
        interfaceListView.setCreator(this);
        interfaceListView.setModel(modelProvider.getModel());
        getContentPanel().add(interfaceListView);
        initWidget(getContentPanel());
        actionButtons = actionPanel.getActionButtons();
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AbstractSubTabPresenter.TYPE_SetActionPanel) {
            getContainer().insert(content, 0);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setMainSelectedItem(VM selectedItem) {
        // Not interested in current selected VM.
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public PatternflyListViewItem<VmNetworkInterface> createListViewItem(VmNetworkInterface selectedItem) {
        return new VmInterfaceListGroupItem(selectedItem, getDetailModel().getGuestAgentData(),
                getDetailModel().getMapNicFilterParameter().get(selectedItem.getId()), actionButtons);
    }
}
