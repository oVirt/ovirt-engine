package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.VmInterfaceActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItemCreator;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class VmInterfaceListModelTable extends AbstractModelBoundTableWidget<VmNetworkInterface, VmInterfaceListModel>
    implements PatternflyListViewItemCreator<VmNetworkInterface> {

    interface WidgetUiBinder extends UiBinder<Widget, VmInterfaceListModelTable> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel interfaceTableContainer;

    @UiField
    PatternflyListView<VM, VmNetworkInterface, VmInterfaceListModel> interfaceListView;

    public VmInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, VmInterfaceListModel> modelProvider,
            EventBus eventBus, VmInterfaceActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);

        interfaceTableContainer.add(actionPanel.getView());

        interfaceListView.setCreator(this);
        interfaceListView.setModel(modelProvider.getModel());
        interfaceListView.setSelectionModel(getTable().getSelectionModel());
    }

    @Override
    protected Widget getWrappedWidget() {
        return WidgetUiBinder.uiBinder.createAndBindUi(this);
    }

    @Override
    public void initTable() {
    }

    @Override
    public PatternflyListViewItem<VmNetworkInterface> createListViewItem(VmNetworkInterface selectedItem) {
        return new VmInterfaceListGroupItem(selectedItem, getModel().getGuestAgentData());
    }
}
