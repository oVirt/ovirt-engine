package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.Comparator;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageTemplateBackupView extends AbstractSubTabTableView<StorageDomain, VmTemplate, StorageListModel, TemplateBackupModel>
        implements SubTabStorageTemplateBackupPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageTemplateBackupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final UIConstants messageConstants = GWT.create(UIConstants.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SubTabStorageTemplateBackupView(SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> modelProvider) {
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

        AbstractTextColumn<VmTemplate> nameColumn =
                new AbstractTextColumn<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> versionNameColumn = new AbstractTextColumn<VmTemplate>() {
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

        AbstractTextColumn<VmTemplate> originColumn =
                new AbstractTextColumn<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return object.getOrigin() == null ? messageConstants.notSpecifiedLabel() : object.getOrigin()
                                .toString();
                    }
                };
        originColumn.makeSortable();
        getTable().addColumn(originColumn, constants.originTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> memoryColumn =
                new AbstractTextColumn<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return messages.megabytes(String.valueOf(object.getMemSizeMb()));
                    }
                };
        memoryColumn.makeSortable();
        getTable().addColumn(memoryColumn, constants.memoryTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> cpuColumn =
                new AbstractTextColumn<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getNumOfCpus());
                    }
                };
        cpuColumn.makeSortable();
        getTable().addColumn(cpuColumn, constants.cpusVm(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> archColumn =
                new AbstractTextColumn<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getClusterArch());
                    }
                };
        archColumn.makeSortable();
        getTable().addColumn(archColumn, constants.architectureVm(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> diskColumn =
                new AbstractTextColumn<VmTemplate>() {
                    @Override
                    public String getValue(VmTemplate object) {
                        return String.valueOf(object.getDiskList().size());
                    }
                };
        diskColumn.makeSortable();
        getTable().addColumn(diskColumn, constants.disksTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> creationDateColumn =
                new AbstractFullDateTimeColumn<VmTemplate>() {
                    @Override
                    protected Date getRawValue(VmTemplate object) {
                        return object.getCreationDate();
                    }
                };
        creationDateColumn.makeSortable(Comparator.comparing(VmTemplate::getCreationDate));
        getTable().addColumn(creationDateColumn, constants.creationDateTemplate(), "160px"); //$NON-NLS-1$

        AbstractTextColumn<VmTemplate> exportDateColumn =
            new AbstractFullDateTimeColumn<VmTemplate>() {
                @Override
                protected Date getRawValue(VmTemplate object) {
                    return object.getExportDate();
                }
            };
        exportDateColumn.makeSortable(Comparator.comparing(VmTemplate::getExportDate));
        getTable().addColumn(exportDateColumn, constants.exportDateTemplate(), "160px"); //$NON-NLS-1$

        getTable().showRefreshButton();
    }

}
