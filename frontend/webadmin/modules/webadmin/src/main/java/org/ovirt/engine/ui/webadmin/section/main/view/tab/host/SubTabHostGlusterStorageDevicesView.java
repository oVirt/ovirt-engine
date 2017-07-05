package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGlusterStorageDevicesPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDeviceStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

public class SubTabHostGlusterStorageDevicesView extends AbstractSubTabTableView<VDS, StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel>
        implements SubTabHostGlusterStorageDevicesPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGlusterStorageDevicesView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabHostGlusterStorageDevicesView(SearchableDetailModelProvider<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    void initTable() {
        getTable().enableColumnResizing();

        StorageDeviceStatusColumn storageDeviceStatusColumn = new StorageDeviceStatusColumn();
        storageDeviceStatusColumn.makeSortable();
        getTable().addColumn(storageDeviceStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDevice> deviceNameColumn =
                new AbstractTextColumn<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getName();
            }
        };
        deviceNameColumn.makeSortable();
        getTable().addColumn(deviceNameColumn, constants.deviceName(), "250px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDevice> descriptionColumn =
                new AbstractTextColumn<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getDescription();
            }
        };

        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.description(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDevice> sizeColumn =
                new AbstractTextColumn<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                    Pair<SizeUnit, Double> convertedSize = SizeConverter.autoConvert(object.getSize(), SizeUnit.MiB);
                    return formatSize(convertedSize.getSecond()) + " " + convertedSize.getFirst().toString(); //$NON-NLS-1$

            }
        };
        sizeColumn.makeSortable();
        getTable().addColumn(sizeColumn, constants.size(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDevice> mountPointColumn =
                new AbstractTextColumn<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getMountPoint();
            }
        };
        mountPointColumn.makeSortable();
        getTable().addColumn(mountPointColumn, constants.mountPoint(), "170px"); //$NON-NLS-1$

        AbstractTextColumn<StorageDevice> fsTypeColumn =
                new AbstractTextColumn<StorageDevice>() {
            @Override
            public String getValue(StorageDevice object) {
                return object.getFsType();
            }
        };
        fsTypeColumn.makeSortable();
        getTable().addColumn(fsTypeColumn, constants.fileSystemType(), "170px"); //$NON-NLS-1$
    }

    public String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);

    }
}
