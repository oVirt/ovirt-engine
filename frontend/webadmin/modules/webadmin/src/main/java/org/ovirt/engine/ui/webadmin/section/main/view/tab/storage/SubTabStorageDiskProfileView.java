package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.inject.Inject;

public class SubTabStorageDiskProfileView extends AbstractSubTabTableView<StorageDomain, DiskProfile, StorageListModel, DiskProfileListModel>
        implements SubTabStorageDiskProfilePresenter.ViewDef {

    @Inject
    public SubTabStorageDiskProfileView(SearchableDetailModelProvider<DiskProfile, StorageListModel, DiskProfileListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<DiskProfile> nameColumn =
                new TextColumnWithTooltip<DiskProfile>() {
                    @Override
                    public String getValue(DiskProfile object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.diskProfileNameLabel(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskProfile> descriptionColumn =
                new TextColumnWithTooltip<DiskProfile>() {
                    @Override
                    public String getValue(DiskProfile object) {
                        return object.getDescription();
                    }
                };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.diskProfileDescriptionLabel(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<DiskProfile> qosColumn = new TextColumnWithTooltip<DiskProfile>() {
            @Override
            public String getValue(DiskProfile object) {
                String name = constants.UnlimitedStorageQos();
                if (object.getQosId() != null) {
                    StorageQos storageQos = getDetailModel().getStorageQos(object.getQosId());
                    if (storageQos != null) {
                        name = storageQos.getName();
                    }
                }
                return name;
            }
        };
        qosColumn.makeSortable();
        getTable().addColumn(qosColumn, constants.storageQosName(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<DiskProfile>(constants.newDiskProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<DiskProfile>(constants.editDiskProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<DiskProfile>(constants.removeDiskProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
