package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageRegisterTemplateView extends AbstractSubTabTableView<StorageDomain, VmTemplate, StorageListModel, StorageRegisterTemplateListModel>
        implements SubTabStorageRegisterTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageRegisterTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SubTabStorageRegisterTemplateView(SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel> modelProvider) {
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

        AbstractTextColumn<VmTemplate> nameColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> originColumn = new AbstractEnumColumn<VmTemplate, OriginType>() {
            @Override
            protected OriginType getRawValue(VmTemplate object) {
                return object.getOrigin();
            }
        };
        originColumn.makeSortable();
        getTable().addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> memoryColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return messages.megabytes(String.valueOf(object.getMemSizeMb()));
            }
        };
        memoryColumn.makeSortable();
        getTable().addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> cpuColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getNumOfCpus());
            }
        };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpusVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> archColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getClusterArch());
            }
        };
        archColumn.makeSortable();
        getTable().addColumn(archColumn, constants.architectureVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> numOfDisksColumn = new AbstractTextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getDiskTemplateMap().size());
            }
        };
        numOfDisksColumn.makeSortable();
        getTable().addColumn(numOfDisksColumn, constants.disksVm(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> creationDateColumn = new AbstractFullDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getCreationDate();
            }
        };
        creationDateColumn.makeSortable();
        getTable().addColumn(creationDateColumn, constants.creationDateVm(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> exportDateColumn = new AbstractFullDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getExportDate();
            }
        };
        exportDateColumn.makeSortable();
        getTable().addColumn(exportDateColumn, constants.exportDateVm(), "200px"); //$NON-NLS-1$
    }
}
