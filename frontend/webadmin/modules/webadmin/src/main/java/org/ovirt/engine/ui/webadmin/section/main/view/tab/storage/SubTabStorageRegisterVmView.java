package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageRegisterVmView extends AbstractSubTabTableView<StorageDomain, VM, StorageListModel, StorageRegisterVmListModel>
        implements SubTabStorageRegisterVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageRegisterVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SubTabStorageRegisterVmView(SearchableDetailModelProvider<VM, StorageListModel, StorageRegisterVmListModel> modelProvider) {
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

        AbstractTextColumn<VM> nameColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VM> originColumn = new AbstractEnumColumn<VM, OriginType>() {
            @Override
            protected OriginType getRawValue(VM object) {
                return object.getOrigin();
            }
        };
        originColumn.makeSortable();
        getTable().addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VM> memoryColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return messages.megabytes(String.valueOf(object.getVmMemSizeMb()));
            }
        };
        memoryColumn.makeSortable();
        getTable().addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VM> cpuColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getNumOfCpus());
            }
        };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpusVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VM> archColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getClusterArch());
            }
        };
        archColumn.makeSortable();
        getTable().addColumn(archColumn, constants.architectureVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VM> numOfDisksColumn = new AbstractTextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return String.valueOf(object.getDiskMap().size());
            }
        };
        getTable().addColumn(numOfDisksColumn, constants.disksVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VM> creationDateColumn = new AbstractFullDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getVmCreationDate();
            }
        };
        getTable().addColumn(creationDateColumn, constants.creationDateVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VM> exportDateColumn = new AbstractFullDateTimeColumn<VM>() {
            @Override
            protected Date getRawValue(VM object) {
                return object.getExportDate();
            }
        };
        getTable().addColumn(exportDateColumn, constants.exportDateVm(), "200px"); //$NON-NLS-1$
    }
}
