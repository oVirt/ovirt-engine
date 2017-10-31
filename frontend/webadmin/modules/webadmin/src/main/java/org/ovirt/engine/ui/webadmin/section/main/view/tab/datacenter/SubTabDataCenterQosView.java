package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.PageHeader;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.CpuQosActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterNetworkQosActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQosPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.HostNetworkQosActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class SubTabDataCenterQosView extends AbstractSubTabTableView<StoragePool,
        StorageQos, DataCenterListModel, DataCenterStorageQosListModel>
        implements SubTabDataCenterQosPresenter.ViewDef {

    private static final int BOTTOM_PADDING = 15;

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterQosView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final VmNetworkQosListModelTable vmNetworkTable;
    private final HostNetworkQosListModelTable hostNetworkTable;
    private final CpuQosListModelTable cpuTable;

    private final SearchableDetailModelProvider<NetworkQoS, DataCenterListModel,
        DataCenterNetworkQoSListModel> vmNetworkModelProvider;
    private final SearchableDetailModelProvider<HostNetworkQos,
        DataCenterListModel, DataCenterHostNetworkQosListModel> hostNetworkModelProvider;
    private final SearchableDetailModelProvider<CpuQos,
        DataCenterListModel, DataCenterCpuQosListModel> cpuModelProvider;

    @Inject
    public SubTabDataCenterQosView(SearchableDetailModelProvider<StorageQos,
            DataCenterListModel, DataCenterStorageQosListModel> modelProvider, SearchableDetailModelProvider<NetworkQoS,
            DataCenterListModel, DataCenterNetworkQoSListModel> vmNetworkModelProvider, SearchableDetailModelProvider<HostNetworkQos,
            DataCenterListModel, DataCenterHostNetworkQosListModel> hostNetworkModelProvider, SearchableDetailModelProvider<CpuQos,
            DataCenterListModel, DataCenterCpuQosListModel> cpuModelProvider, CpuQosActionPanelPresenterWidget cpuQosActionPanel,
            DataCenterNetworkQosActionPanelPresenterWidget vmNetworkQosActionPanel,
            HostNetworkQosActionPanelPresenterWidget hostQosActionPanel,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider);
        this.vmNetworkModelProvider = vmNetworkModelProvider;
        this.hostNetworkModelProvider = hostNetworkModelProvider;
        this.cpuModelProvider = cpuModelProvider;
        initTable();
        initWidget(getTableContainer());
        vmNetworkTable = new VmNetworkQosListModelTable(vmNetworkModelProvider, eventBus, vmNetworkQosActionPanel, clientStorage);
        hostNetworkTable = new HostNetworkQosListModelTable(hostNetworkModelProvider, eventBus, hostQosActionPanel, clientStorage);
        cpuTable = new CpuQosListModelTable(cpuModelProvider, eventBus, cpuQosActionPanel, clientStorage);
        if (getTableContainer() instanceof FlowPanel) {
            FlowPanel container = (FlowPanel) getTableContainer();
            PageHeader vmNetworkHeader = new PageHeader();
            vmNetworkHeader.setText(constants.dataCenterNetworkQoSSubTabLabel());
            container.add(vmNetworkHeader);
            container.add(vmNetworkTable);
            PageHeader hostNetworkHeader = new PageHeader();
            hostNetworkHeader.setText(constants.dataCenterHostNetworkQosSubTabLabel());
            container.add(hostNetworkHeader);
            container.add(hostNetworkTable);
            PageHeader cpuHeader = new PageHeader();
            cpuHeader.setText(constants.dataCenterCpuQosSubTabLabel());
            container.add(cpuHeader);
            container.add(cpuTable);
            container.getElement().getStyle().setPaddingBottom(BOTTOM_PADDING, Unit.PX);
        }
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        super.setInSlot(slot, content);
        if (slot == AbstractSubTabPresenter.TYPE_SetActionPanel) {
            if (getTableContainer() instanceof FlowPanel) {
                FlowPanel container = (FlowPanel) getTableContainer();
                PageHeader storageHeader = new PageHeader();
                storageHeader.setText(constants.dataCenterStorageQosSubTabLabel());
                container.insert(storageHeader, 0);
            }
        }
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<StorageQos> nameColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getName() == null ? "" : object.getName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.qosName(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> descColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.qosDescription(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> throughputColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxThroughput() == null ? constants.unlimitedQos()
                        : object.getMaxThroughput().toString();
            }
        };
        throughputColumn.makeSortable();
        getTable().addColumn(throughputColumn, constants.storageQosThroughputTotal(), "155px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> readThroughputColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxReadThroughput() == null ? constants.unlimitedQos()
                        : object.getMaxReadThroughput().toString();
            }
        };
        readThroughputColumn.makeSortable();
        getTable().addColumn(readThroughputColumn, constants.storageQosThroughputRead(), "155px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> writeThroughputColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxWriteThroughput() == null ? constants.unlimitedQos()
                        : object.getMaxWriteThroughput().toString();
            }
        };
        writeThroughputColumn.makeSortable();
        getTable().addColumn(writeThroughputColumn, constants.storageQosThroughputWrite(), "155px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> iopsColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxIops() == null ? constants.unlimitedQos()
                        : object.getMaxIops().toString();
            }
        };
        iopsColumn.makeSortable();
        getTable().addColumn(iopsColumn, constants.storageQosIopsTotal(), "105px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> readIopsColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxReadIops() == null ? constants.unlimitedQos()
                        : object.getMaxReadIops().toString();
            }
        };
        readIopsColumn.makeSortable();
        getTable().addColumn(readIopsColumn, constants.storageQosIopsRead(), "105px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> writeIopsColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxWriteIops() == null ? constants.unlimitedQos()
                        : object.getMaxWriteIops().toString();
            }
        };
        writeIopsColumn.makeSortable();
        getTable().addColumn(writeIopsColumn, constants.storageQosIopsWrite(), "105px"); //$NON-NLS-1$
    }

    @Override
    public void onAttach() {
        super.onAttach();
        // This might look strange as I also have a call to resizeContainerToHeight from the presenter onReveal.
        // However for some reason the absoluteTop is reported wrong the first time that method is called.
        // To mitigate that problem, I have both onReveal and onAttach which will call resizeContainerToHeight twice
        // when the detail tab is revealed, the second which will report the absolute height correctly and make it
        // work correctly.
        resizeContainerToHeight();
    }

    @Override
    public void resizeContainerToHeight() {
        int height = Window.getClientHeight() - getTableContainer().asWidget().getAbsoluteTop();
        getTableContainer().asWidget().getElement().getStyle().setHeight(height, Unit.PX);
        getTableContainer().asWidget().getElement().getStyle().setOverflow(Overflow.AUTO);
    }
}
