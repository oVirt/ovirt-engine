package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostDevicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

public class SubTabHostDeviceView
        extends AbstractSubTabTableView<VDS, HostDeviceView, HostListModel<Void>, HostDeviceListModel>
        implements SubTabHostDevicePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostDeviceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabHostDeviceView(SearchableDetailModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    public void initTable() {
        getTable().enableColumnResizing();

        addColumn(constants.deviceName(), "350px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getDeviceName();
            }
        });
        addColumn(constants.iommuGroup(), "100px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getIommuGroup() == null ? constants.notAvailableLabel() : object.getIommuGroup().toString();
            }
        });
        addColumn(constants.capability(), "100px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
            @Override
            public String getValue(HostDeviceView object) {
                return object.getCapability();
            }
        });
        addColumn(constants.product(), "350px", new AbstractTextColumn<HostDeviceView>() { //$NON-NLS-1$
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

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
