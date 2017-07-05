package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGlusterSwiftPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabHostGlusterSwiftView extends AbstractSubTabTableView<VDS, GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel>
        implements SubTabHostGlusterSwiftPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGlusterSwiftView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabHostGlusterSwiftView(SearchableDetailModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<GlusterServerService> serviceColumn = new AbstractTextColumn<GlusterServerService>() {
            @Override
            public String getValue(GlusterServerService object) {
                return object.getServiceName();
            }
        };
        serviceColumn.makeSortable();
        getTable().addColumn(serviceColumn, constants.serviceGlusterSwift(), "250px"); //$NON-NLS-1$

        AbstractTextColumn<GlusterServerService> statusColumn =
            new AbstractEnumColumn<GlusterServerService, GlusterServiceStatus>() {
                @Override
                protected GlusterServiceStatus getRawValue(GlusterServerService object) {
                    return object.getStatus();
                }
            };
        statusColumn.makeSortable();
        getTable().addColumn(statusColumn, constants.statusGlusterSwift(), "250px"); //$NON-NLS-1$
    }

}
