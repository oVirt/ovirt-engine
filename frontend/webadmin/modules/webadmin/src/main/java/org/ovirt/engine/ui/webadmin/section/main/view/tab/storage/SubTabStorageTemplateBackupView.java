package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
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

public class SubTabStorageTemplateBackupView extends AbstractSubTabTableView<storage_domains, VmTemplate, StorageListModel, TemplateBackupModel>
        implements SubTabStorageTemplateBackupPresenter.ViewDef {

    private static final Constants messageConstants = GWT.create(Constants.class);

    @Inject
    public SubTabStorageTemplateBackupView(SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        TextColumnWithTooltip<VmTemplate> nameColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getname();
                    }
                };
        getTable().addColumn(nameColumn, constants.nameTemplate());

        TextColumnWithTooltip<VmTemplate> originColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getOrigin() == null ? messageConstants.notSpecifiedLabel() : object.getOrigin()
                                .toString();
                    }
                };
        getTable().addColumn(originColumn, constants.originTemplate());

        TextColumnWithTooltip<VmTemplate> memoryColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getMemSizeMb()) + " MB"; //$NON-NLS-1$
                    }
                };
        getTable().addColumn(memoryColumn,constants.memoryTemplate());

        TextColumnWithTooltip<VmTemplate> cpuColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getNumOfCpus());
                    }
                };
        getTable().addColumn(cpuColumn, constants.cpusVm());

        TextColumnWithTooltip<VmTemplate> diskColumn =
                new TextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getDiskList().size());
                    }
                };
        getTable().addColumn(diskColumn, constants.disksTemplate());

        TextColumnWithTooltip<VmTemplate> creationDateColumn =
                new GeneralDateTimeColumn<VmTemplate>() {
                    @Override
                    protected Date getRawValue(VmTemplate object) {
                        return object.getCreationDate();
                    }
                };
        getTable().addColumn(creationDateColumn, constants.creationDateTemplate());

        TextColumnWithTooltip<VmTemplate> exportDateColumn =
            new GeneralDateTimeColumn<VmTemplate>() {
                @Override
                protected Date getRawValue(VmTemplate object) {
                    return object.getExportDate();
                }
            };
        getTable().addColumn(exportDateColumn, constants.exportDateTemplate());

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
