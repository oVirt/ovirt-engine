package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.VmHostDeviceListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

public class VmHostDeviceModelTable extends AbstractModelBoundTableWidget<HostDeviceView, VmHostDeviceListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VmHostDeviceModelTable(SearchableTableModelProvider<HostDeviceView, VmHostDeviceListModel> modelProvider, EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getEventBus(), constants.addVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getEventBus(), constants.removeVmHostDevice()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<HostDeviceView>(getEventBus(), constants.repinVmHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRepinHostCommand();
            }
        });

        getTable().enableColumnResizing();

        addColumn(constants.deviceName(), "150px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getDeviceName();
            }
        });
        addColumn(constants.iommuGroup(), "150px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getIommuGroup() == null ? constants.notAvailableLabel() : object.getIommuGroup().toString();
            }
        });
        addColumn(constants.capability(), "130px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getCapability();
            }
        });
        addColumn(constants.productName(), "150px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getProductName();
            }
        });
        addColumn(constants.productId(), "150px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getProductId();
            }
        });
        addColumn(constants.vendorName(), "150px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getVendorName();
            }
        });
        addColumn(constants.vendorId(), "150px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getVendorId();
            }
        });
    }

    private void addColumn(String header, String width, AbstractTextColumn<HostDeviceView> column) {
        column.makeSortable();
        getTable().addColumn(column, header, width);
    }
}
