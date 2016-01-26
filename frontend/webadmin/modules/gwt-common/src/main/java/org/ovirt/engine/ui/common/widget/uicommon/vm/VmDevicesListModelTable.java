package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.VmDeviceGeneralTypeColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;
import com.google.gwt.event.shared.EventBus;

public class VmDevicesListModelTable extends AbstractModelBoundTableWidget<VmDevice, VmDevicesListModel<VM>> {
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public VmDevicesListModelTable(
            SearchableTableModelProvider<VmDevice, VmDevicesListModel<VM>> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        addGeneralTypeColumn();
        addDeviceTypeColumn();
        addAddressColumn();
        addBootOrderColumn();
        addReadOnlyColumn();
        addPluggedColumn();
        addManagedColumn();
        addSpecParamsColumn();
    }

    private void addGeneralTypeColumn() {
        final VmDeviceGeneralTypeColumn<VmDevice> deviceGeneralTypeColumn = new VmDeviceGeneralTypeColumn<>();
        deviceGeneralTypeColumn.setContextMenuTitle(constants.deviceGeneralType());
        deviceGeneralTypeColumn.makeSortable();
        getTable().addColumn(deviceGeneralTypeColumn, constants.empty(), "30px"); //$NON-NLS-1$
    }

    private void addDeviceTypeColumn() {
        final AbstractTextColumn<VmDevice> deviceTypeColumn = new AbstractTextColumn<VmDevice>() {
            @Override
            public String getValue(VmDevice device) {
                return device.getDevice();
            }
        };
        deviceTypeColumn.makeSortable();
        getTable().addColumn(deviceTypeColumn, constants.deviceType(), "70px"); //$NON-NLS-1$
    }

    private void addAddressColumn() {
        final AbstractTextColumn<VmDevice> deviceAddressColumn = new AbstractTextColumn<VmDevice>() {
            @Override
            public String getValue(VmDevice device) {
                return device.getAddress();
            }
        };
        deviceAddressColumn.makeSortable();
        getTable().addColumn(deviceAddressColumn, constants.deviceAddress(), "395px"); //$NON-NLS-1$
    }

    private void addReadOnlyColumn() {
        final AbstractCheckboxColumn<VmDevice> readonlyColumn = new AbstractCheckboxColumn<VmDevice>() {
            @Override
            public Boolean getValue(VmDevice object) {
                return object.getIsReadOnly();
            }

            @Override
            protected boolean canEdit(VmDevice object) {
                return false;
            }
        };
        readonlyColumn.makeSortable();
        getTable().addColumn(readonlyColumn, constants.deviceReadOnlyAlias(), "70px"); //$NON-NLS-1$
    }

    private void addPluggedColumn() {
        final AbstractCheckboxColumn<VmDevice> pluggedColumn = new AbstractCheckboxColumn<VmDevice>() {
            @Override
            public Boolean getValue(VmDevice object) {
                return object.getIsPlugged();
            }

            @Override
            protected boolean canEdit(VmDevice object) {
                return false;
            }
        };
        pluggedColumn.makeSortable();
        getTable().addColumn(pluggedColumn, constants.devicePluggedAlias(), "70px"); //$NON-NLS-1$
    }

    private void addManagedColumn() {
        final AbstractCheckboxColumn<VmDevice> managedColumn = new AbstractCheckboxColumn<VmDevice>() {
            @Override
            public Boolean getValue(VmDevice object) {
                return object.getIsManaged();
            }

            @Override
            protected boolean canEdit(VmDevice object) {
                return false;
            }
        };
        managedColumn.makeSortable();
        getTable().addColumn(managedColumn, constants.deviceManagedAlias(), "70px"); //$NON-NLS-1$
    }

    private void addBootOrderColumn() {
        final AbstractTextColumn<VmDevice> bootOrderColumn = new AbstractTextColumn<VmDevice>() {
            @Override
            public String getValue(VmDevice device) {
                if (device.getBootOrder() != 0) {
                    return String.valueOf(device.getBootOrder());
                } else {
                    return ""; //$NON-NLS-1$
                }
            }
        };
        bootOrderColumn.makeSortable();
        getTable().addColumn(bootOrderColumn , constants.deviceBootOrderAlias(), "70px"); //$NON-NLS-1$
    }

    private void addSpecParamsColumn() {
        final AbstractTextColumn<VmDevice> specParamsColumn = new AbstractTextColumn<VmDevice>() {
            @Override
            public String getValue(VmDevice device) {
                return device.getSpecParams().toString();
            }
        };
        specParamsColumn.makeSortable();
        getTable().addColumn(specParamsColumn , constants.deviceSpecParamsAlias(), "300px"); //$NON-NLS-1$
    }

}
