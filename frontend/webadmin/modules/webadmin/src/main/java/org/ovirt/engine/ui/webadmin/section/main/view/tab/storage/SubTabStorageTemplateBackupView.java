package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageTemplateBackupView extends AbstractSubTabTableView<StorageDomain, VmTemplate, StorageListModel, TemplateBackupModel>
        implements SubTabStorageTemplateBackupPresenter.ViewDef {

    private static final Constants messageConstants = GWT.create(Constants.class);

    @Inject
    public SubTabStorageTemplateBackupView(SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VmTemplate> nameColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.nameTemplate(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> originColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getOrigin() == null ? messageConstants.notSpecifiedLabel() : object.getOrigin()
                                .toString();
                    }
                };
        getTable().addColumn(originColumn, constants.originTemplate(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> memoryColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getMemSizeMb()) + " MB"; //$NON-NLS-1$
                    }
                };
        getTable().addColumn(memoryColumn, constants.memoryTemplate(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> cpuColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getNumOfCpus());
                    }
                };
        getTable().addColumn(cpuColumn, constants.cpusVm(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> diskColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getDiskList().size());
                    }
                };
        getTable().addColumn(diskColumn, constants.disksTemplate(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> creationDateColumn =
                new GeneralDateTimeColumn<VmTemplate>() {
                    @Override
                    protected Date getRawValue(VmTemplate object) {
                        return object.getCreationDate();
                    }
                };
        getTable().addColumn(creationDateColumn, constants.creationDateTemplate(), "160px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmTemplate> exportDateColumn =
            new GeneralDateTimeColumn<VmTemplate>() {
                @Override
                protected Date getRawValue(VmTemplate object) {
                    return object.getExportDate();
                }
            };
        getTable().addColumn(exportDateColumn, constants.exportDateTemplate(), "160px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VmTemplate>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestoreCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<VmTemplate>(constants.removeTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
