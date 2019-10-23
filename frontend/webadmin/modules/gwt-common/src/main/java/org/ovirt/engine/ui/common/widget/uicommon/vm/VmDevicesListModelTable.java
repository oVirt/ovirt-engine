package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.HasCellClickHandlers;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.VmDeviceGeneralTypeColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDeviceFeEntity;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDevicesListModel;

import com.google.gwt.event.shared.EventBus;

public class VmDevicesListModelTable extends AbstractModelBoundTableWidget<VM, VmDeviceFeEntity, VmDevicesListModel<VM>> {
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private HotUnplugColumn hotUnplugColumn;

    public VmDevicesListModelTable(
            SearchableTableModelProvider<VmDeviceFeEntity, VmDevicesListModel<VM>> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        // No action panel for vm devices list model table, passing null.
        super(modelProvider, eventBus, null, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        addGeneralTypeColumn();
        addDeviceTypeColumn();
        addAddressColumn();
        addReadOnlyColumn();
        addPluggedColumn();
        addManagedColumn();
        addSpecParamsColumn();
        addHotUnplugColumn();

    }

    private void addGeneralTypeColumn() {
        final VmDeviceGeneralTypeColumn<VmDeviceFeEntity> deviceGeneralTypeColumn = new VmDeviceGeneralTypeColumn<>();
        deviceGeneralTypeColumn.setContextMenuTitle(constants.deviceGeneralType());
        deviceGeneralTypeColumn.makeSortable();
        getTable().addColumn(deviceGeneralTypeColumn, constants.empty(), "30px"); //$NON-NLS-1$
    }

    private void addDeviceTypeColumn() {
        final AbstractTextColumn<VmDeviceFeEntity> deviceTypeColumn = new AbstractTextColumn<VmDeviceFeEntity>() {
            @Override
            public String getValue(VmDeviceFeEntity deviceEntity) {
                return deviceEntity.getVmDevice().getDevice();
            }
        };
        deviceTypeColumn.makeSortable();
        getTable().addColumn(deviceTypeColumn, constants.deviceType(), "70px"); //$NON-NLS-1$
    }

    private void addAddressColumn() {
        final AbstractTextColumn<VmDeviceFeEntity> deviceAddressColumn = new AbstractTextColumn<VmDeviceFeEntity>() {
            @Override
            public String getValue(VmDeviceFeEntity deviceEntity) {
                return deviceEntity.getVmDevice().getAddress();
            }
        };
        deviceAddressColumn.makeSortable();
        getTable().addColumn(deviceAddressColumn, constants.deviceAddress(), "395px"); //$NON-NLS-1$
    }

    private void addReadOnlyColumn() {
        final AbstractCheckboxColumn<VmDeviceFeEntity> readonlyColumn = new AbstractCheckboxColumn<VmDeviceFeEntity>() {
            @Override
            public Boolean getValue(VmDeviceFeEntity object) {
                return object.getVmDevice().getReadOnly();
            }

            @Override
            protected boolean canEdit(VmDeviceFeEntity object) {
                return false;
            }
        };
        readonlyColumn.makeSortable();
        getTable().addColumn(readonlyColumn, constants.deviceReadOnlyAlias(), "70px"); //$NON-NLS-1$
    }

    private void addPluggedColumn() {
        final AbstractCheckboxColumn<VmDeviceFeEntity> pluggedColumn = new AbstractCheckboxColumn<VmDeviceFeEntity>() {
            @Override
            public Boolean getValue(VmDeviceFeEntity object) {
                return object.getVmDevice().isPlugged();
            }

            @Override
            protected boolean canEdit(VmDeviceFeEntity object) {
                return false;
            }
        };
        pluggedColumn.makeSortable();
        getTable().addColumn(pluggedColumn, constants.devicePluggedAlias(), "70px"); //$NON-NLS-1$
    }

    private void addManagedColumn() {
        final AbstractCheckboxColumn<VmDeviceFeEntity> managedColumn = new AbstractCheckboxColumn<VmDeviceFeEntity>() {
            @Override
            public Boolean getValue(VmDeviceFeEntity object) {
                return object.getVmDevice().isManaged();
            }

            @Override
            protected boolean canEdit(VmDeviceFeEntity object) {
                return false;
            }
        };
        managedColumn.makeSortable();
        getTable().addColumn(managedColumn, constants.deviceManagedAlias(), "70px"); //$NON-NLS-1$
    }

    private void addSpecParamsColumn() {
        final AbstractTextColumn<VmDeviceFeEntity> specParamsColumn = new AbstractTextColumn<VmDeviceFeEntity>() {
            @Override
            public String getValue(VmDeviceFeEntity deviceEntity) {
                return deviceEntity.getVmDevice().getSpecParams().toString();
            }
        };
        specParamsColumn.makeSortable();
        getTable().addColumn(specParamsColumn , constants.deviceSpecParamsAlias(), "300px"); //$NON-NLS-1$
    }

    private void addHotUnplugColumn() {
        hotUnplugColumn = new HotUnplugColumn();
        getTable().addColumn(hotUnplugColumn , constants.hotUnplug(), "98px"); //$NON-NLS-1$
    }

    public HasCellClickHandlers<VmDeviceFeEntity> getHotUnplugColumn() {
        return hotUnplugColumn;
    }

}
