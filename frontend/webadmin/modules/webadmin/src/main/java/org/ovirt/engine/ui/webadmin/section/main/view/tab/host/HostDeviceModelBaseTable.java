package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModelBase;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.HostDeviceColumnHelper;

import com.google.gwt.event.shared.EventBus;

public abstract class HostDeviceModelBaseTable<M extends HostDeviceListModelBase<?>> extends AbstractModelBoundTableWidget<HostDeviceView, M> {

    protected static final ApplicationConstants constants = AssetProvider.getConstants();

    public HostDeviceModelBaseTable(SearchableTableModelProvider<HostDeviceView, M> modelProvider, EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {

        getTable().enableColumnResizing();

        addColumn(constants.deviceName(), "200px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getDeviceName();
            }
        });
        addColumn(constants.capability(), "130px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getCapability();
            }
        });
        addColumn(constants.vendor(), "200px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return HostDeviceColumnHelper.renderNameId(object.getVendorName(), object.getVendorId());
            }
        });
        addColumn(constants.product(), "350px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return HostDeviceColumnHelper.renderNameId(object.getProductName(), object.getProductId());
            }
        });
        addColumn(constants.driver(), "100px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView hostDeviceView) {
                return Objects.toString(hostDeviceView.getDriver(), "");
            }
        });
        addColumn(constants.currentlyUsedByVm(), "120px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getRunningVmName();
            }
        });
        addColumn(constants.attachedToVms(), "120px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return HostDeviceColumnHelper.renderVmNamesList(object.getAttachedVmNames());
            }
        });
        addColumn(constants.iommuGroup(), "120px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return HostDeviceColumnHelper.renderIommuGroup(object.getIommuGroup());
            }
        });
    }

    private void addColumn(String header, String width, AbstractTextColumn<HostDeviceView> column) {
        column.makeSortable();
        getTable().addColumn(column, header, width);
    }
}
