package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGlusterStorageDevicesPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDeviceStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

public class SubTabHostGlusterStorageDevicesView extends AbstractSubTabTableView<VDS, StorageDevice, HostListModel, HostGlusterStorageDevicesListModel>
        implements SubTabHostGlusterStorageDevicesPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGlusterStorageDevicesView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabHostGlusterStorageDevicesView(SearchableDetailModelProvider<StorageDevice, HostListModel, HostGlusterStorageDevicesListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        StorageDeviceStatusColumn storageDeviceStatusColumn = new StorageDeviceStatusColumn();
        storageDeviceStatusColumn.makeSortable();
        getTable().addColumn(storageDeviceStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<StorageDevice> deviceNameColumn =
                new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getName();
            }
        };
        deviceNameColumn.makeSortable();
        getTable().addColumn(deviceNameColumn, constants.deviceName(), "250px"); //$NON-NLS-1$

        TextColumnWithTooltip<StorageDevice> descriptionColumn =
                new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getDescription();
            }
        };

        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.description(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<StorageDevice> sizeColumn =
                new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                    Pair<SizeUnit, Double> convertedSize = SizeConverter.autoConvert(object.getSize(), SizeUnit.MB);
                    return formatSize(convertedSize.getSecond()) + " " + convertedSize.getFirst().toString(); //$NON-NLS-1$

            }
        };
        sizeColumn.makeSortable();
        getTable().addColumn(sizeColumn, constants.size(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<StorageDevice> mountPointColumn =
                new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getMountPoint();
            }
        };
        mountPointColumn.makeSortable();
        getTable().addColumn(mountPointColumn, constants.mountPoint(), "170px"); //$NON-NLS-1$

        TextColumnWithTooltip<StorageDevice> fsTypeColumn =
                new TextColumnWithTooltip<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getFsType();
            }
        };
        fsTypeColumn.makeSortable();
        getTable().addColumn(fsTypeColumn, constants.fileSystemType(), "170px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<StorageDevice>(constants.syncStorageDevices()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSyncStorageDevicesCommand();
            }
        });

    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }

}
