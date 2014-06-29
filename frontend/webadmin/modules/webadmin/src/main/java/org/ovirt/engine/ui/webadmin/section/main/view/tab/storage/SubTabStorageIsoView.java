package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.DiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.inject.Inject;

public class SubTabStorageIsoView extends AbstractSubTabTableView<StorageDomain, RepoImage, StorageListModel, StorageIsoListModel>
        implements SubTabStorageIsoPresenter.ViewDef {

    @Inject
    public SubTabStorageIsoView(SearchableDetailModelProvider<RepoImage, StorageListModel, StorageIsoListModel> modelProvider,
            ApplicationResources resources, ApplicationConstants constants) {
        super(modelProvider);
        initTable(resources, constants);
        initWidget(getTable());
    }

    void initTable(ApplicationResources resources, final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<RepoImage> fileNameColumn = new TextColumnWithTooltip<RepoImage>() {
            @Override
            public String getValue(RepoImage object) {
                return object.getRepoImageTitle();
            }
        };
        fileNameColumn.makeSortable();
        getTable().addColumn(fileNameColumn, constants.fileNameIso(), "500px"); //$NON-NLS-1$

        TextColumnWithTooltip<RepoImage> typeColumn = new TextColumnWithTooltip<RepoImage>() {
            @Override
            public String getValue(RepoImage object) {
                return object.getFileType().toString();
            }
        };
        typeColumn.makeSortable();
        getTable().addColumn(typeColumn, constants.typeIso(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<RepoImage> sizeColumn = new DiskSizeColumn<RepoImage>(SizeConverter.SizeUnit.BYTES,
                DiskSizeRenderer.Format.HUMAN_READABLE) {
            @Override
            protected Long getRawValue(RepoImage object) {
                return object.getSize();
            }
        };
        sizeColumn.makeSortable();
        getTable().addColumn(sizeColumn, constants.actualSizeTemplate(), "100px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<RepoImage>(constants.importImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getImportImagesCommand();
            }
        });

        getTable().showRefreshButton();
    }
}
