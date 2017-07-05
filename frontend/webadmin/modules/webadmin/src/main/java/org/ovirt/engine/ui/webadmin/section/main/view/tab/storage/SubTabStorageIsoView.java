package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDiskSizeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabStorageIsoView extends AbstractSubTabTableView<StorageDomain, RepoImage, StorageListModel, StorageIsoListModel>
        implements SubTabStorageIsoPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageIsoView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabStorageIsoView(SearchableDetailModelProvider<RepoImage, StorageListModel, StorageIsoListModel> modelProvider) {
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

        AbstractTextColumn<RepoImage> fileNameColumn = new AbstractTextColumn<RepoImage>() {
            @Override
            public String getValue(RepoImage object) {
                return object.getRepoImageTitle();
            }
        };
        fileNameColumn.makeSortable();
        getTable().addColumn(fileNameColumn, constants.fileNameIso(), "500px"); //$NON-NLS-1$

        AbstractTextColumn<RepoImage> typeColumn = new AbstractTextColumn<RepoImage>() {
            @Override
            public String getValue(RepoImage object) {
                return object.getFileType().toString();
            }
        };
        typeColumn.makeSortable();
        getTable().addColumn(typeColumn, constants.typeIso(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<RepoImage> sizeColumn = new AbstractDiskSizeColumn<RepoImage>(SizeConverter.SizeUnit.BYTES,
                DiskSizeRenderer.Format.HUMAN_READABLE) {
            @Override
            protected Long getRawValue(RepoImage object) {
                return object.getSize();
            }
        };
        sizeColumn.makeSortable();
        getTable().addColumn(sizeColumn, constants.actualSizeTemplate(), "100px"); //$NON-NLS-1$

        getTable().showRefreshButton();
    }
}
