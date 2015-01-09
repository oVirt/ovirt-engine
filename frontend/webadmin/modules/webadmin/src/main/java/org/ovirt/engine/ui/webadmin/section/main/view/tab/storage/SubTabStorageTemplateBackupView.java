package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractGeneralDateTimeColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageTemplateBackupView extends AbstractSubTabTableView<StorageDomain, VmTemplate, StorageListModel, TemplateBackupModel>
        implements SubTabStorageTemplateBackupPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageTemplateBackupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final UIConstants messageConstants = GWT.create(UIConstants.class);

    @Inject
    public SubTabStorageTemplateBackupView(SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        AbstractTextColumnWithTooltip<VmTemplate> nameColumn =
                new AbstractTextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> versionNameColumn = new AbstractTextColumnWithTooltip<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                if (object.isBaseTemplate()) {
                    return ""; //$NON-NLS-1$
                }

                return StringFormat.format("%s (%s)", //$NON-NLS-1$
                        object.getTemplateVersionName() != null ? object.getTemplateVersionName() : "", //$NON-NLS-1$
                        object.getTemplateVersionNumber());
            }
        };
        versionNameColumn.makeSortable();
        table.addColumn(versionNameColumn, constants.versionTemplate(), "150px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> originColumn =
                new AbstractTextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getOrigin() == null ? messageConstants.notSpecifiedLabel() : object.getOrigin()
                                .toString();
                    }
                };
        originColumn.makeSortable();
        getTable().addColumn(originColumn, constants.originTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> memoryColumn =
                new AbstractTextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getMemSizeMb()) + " MB"; //$NON-NLS-1$
                    }
                };
        memoryColumn.makeSortable();
        getTable().addColumn(memoryColumn, constants.memoryTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> cpuColumn =
                new AbstractTextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getNumOfCpus());
                    }
                };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpusVm(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> archColumn =
                new AbstractTextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getClusterArch());
                    }
                };
        archColumn.makeSortable();
        getTable().addColumn(archColumn, constants.architectureVm(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> diskColumn =
                new AbstractTextColumnWithTooltip<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getDiskList().size());
                    }
                };
        diskColumn.makeSortable();
        getTable().addColumn(diskColumn, constants.disksTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> creationDateColumn =
                new AbstractGeneralDateTimeColumn<VmTemplate>() {
                    @Override
                    protected Date getRawValue(VmTemplate object) {
                        return object.getCreationDate();
                    }
                };
        creationDateColumn.makeSortable();
        getTable().addColumn(creationDateColumn, constants.creationDateTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VmTemplate> exportDateColumn =
            new AbstractGeneralDateTimeColumn<VmTemplate>() {
                @Override
                protected Date getRawValue(VmTemplate object) {
                    return object.getExportDate();
                }
            };
        exportDateColumn.makeSortable();
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

        getTable().showRefreshButton();
    }

}
