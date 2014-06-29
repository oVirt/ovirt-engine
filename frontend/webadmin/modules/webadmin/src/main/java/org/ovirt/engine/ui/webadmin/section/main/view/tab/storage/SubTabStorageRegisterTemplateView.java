package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;

import com.google.inject.Inject;

public class SubTabStorageRegisterTemplateView extends AbstractSubTabTableView<StorageDomain, VmTemplate, StorageListModel, StorageRegisterTemplateListModel>
        implements SubTabStorageRegisterTemplatePresenter.ViewDef {

    @Inject
    public SubTabStorageRegisterTemplateView(SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VmTemplate> nameColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVm(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> originColumn = new EnumColumn<VmTemplate, OriginType>() {
            @Override
            protected OriginType getRawValue(VmTemplate object) {
                return object.getOrigin();
            }
        };
        originColumn.makeSortable();
        getTable().addColumn(originColumn, constants.originVm(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> memoryColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getMemSizeMb()) + " MB"; //$NON-NLS-1$
            }
        };
        memoryColumn.makeSortable();
        getTable().addColumn(memoryColumn, constants.memoryVm(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> cpuColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getNumOfCpus());
            }
        };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpusVm(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> archColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getClusterArch());
            }
        };
        archColumn.makeSortable();
        getTable().addColumn(archColumn, constants.architectureVm(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> numOfDisksColumn = new TextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return String.valueOf(object.getDiskTemplateMap().size());
            }
        };
        numOfDisksColumn.makeSortable();
        getTable().addColumn(numOfDisksColumn, constants.disksVm(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> creationDateColumn = new GeneralDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getCreationDate();
            }
        };
        creationDateColumn.makeSortable();
        getTable().addColumn(creationDateColumn, constants.creationDateVm(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> exportDateColumn = new GeneralDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getExportDate();
            }
        };
        exportDateColumn.makeSortable();
        getTable().addColumn(exportDateColumn, constants.exportDateVm(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VmTemplate>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getImportCommand();
            }
        });
    }
}
