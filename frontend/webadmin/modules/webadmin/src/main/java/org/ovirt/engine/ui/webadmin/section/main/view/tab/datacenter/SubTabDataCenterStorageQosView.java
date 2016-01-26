package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStorageQosPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;

public class SubTabDataCenterStorageQosView extends AbstractSubTabTableView<StoragePool,
        StorageQos, DataCenterListModel, DataCenterStorageQosListModel>
        implements SubTabDataCenterStorageQosPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterStorageQosView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterStorageQosView(SearchableDetailModelProvider<StorageQos,
            DataCenterListModel, DataCenterStorageQosListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
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
        getTable().addColumn(throughputColumn, constants.storageQosThroughputTotal(), "105px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> readThroughputColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxReadThroughput() == null ? constants.unlimitedQos()
                        : object.getMaxReadThroughput().toString();
            }
        };
        readThroughputColumn.makeSortable();
        getTable().addColumn(readThroughputColumn, constants.storageQosThroughputRead(), "105px"); //$NON-NLS-1$

        AbstractTextColumn<StorageQos> writeThroughputColumn = new AbstractTextColumn<StorageQos>() {
            @Override
            public String getValue(StorageQos object) {
                return object.getMaxWriteThroughput() == null ? constants.unlimitedQos()
                        : object.getMaxWriteThroughput().toString();
            }
        };
        writeThroughputColumn.makeSortable();
        getTable().addColumn(writeThroughputColumn, constants.storageQosThroughputWrite(), "105px"); //$NON-NLS-1$

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

        getTable().addActionButton(new WebAdminButtonDefinition<StorageQos>(constants.newQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<StorageQos>(constants.editQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<StorageQos>(constants.removeQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }
}
