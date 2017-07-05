package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterIscsiBondListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterIscsiBondPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabDataCenterIscsiBondView extends AbstractSubTabTableView<StoragePool, IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel>
        implements SubTabDataCenterIscsiBondPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterIscsiBondView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterIscsiBondView(SearchableDetailModelProvider<IscsiBond, DataCenterListModel,
            DataCenterIscsiBondListModel> modelProvider) {
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

        getTable().addColumn(new AbstractTextColumn<IscsiBond>() {
            @Override
            public String getValue(IscsiBond object) {
                return object.getName();
            }
        }, constants.name(), "400px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<IscsiBond>() {
            @Override
            public String getValue(IscsiBond object) {
                return object.getDescription();
            }
        }, constants.description(), "400px"); //$NON-NLS-1$
    }
}
