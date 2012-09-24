package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.TextCellWithEditableTooltip;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithEditableTooltip;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaStorageView extends AbstractSubTabTableView<Quota, QuotaStorage, QuotaListModel, QuotaStorageListModel>
        implements SubTabQuotaStoragePresenter.ViewDef {

    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<Number>(DiskSizeRenderer.DiskSizeUnit.GIGABYTE);

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaStorageView(SearchableDetailModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    private void initTable(final ApplicationConstants constants) {
        getTable().addColumn(new TextColumnWithTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                return object.getStorageName() == null || object.getStorageName().equals("") ? constants.utlQuotaAllStoragesQuotaPopup()
                        : object.getStorageName();
            }
        },
                constants.nameQuotaStorage());

        getTable().addColumn(new TextColumnWithEditableTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                String str;
                if (object.getStorageSizeGB() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getStorageSizeGB().equals(QuotaStorage.UNLIMITED)) {
                    str = constants.outOfQuota() + constants.unlimitedQuota();
                } else {
                    str = constants.outOfQuota() + diskSizeRenderer.render(object.getStorageSizeGB());
                }
                return (object.getStorageSizeGBUsage() == 0 ? 0
                        : diskSizeRenderer.render(object.getStorageSizeGBUsage())) + str;
            }

            @Override
            public TextCellWithEditableTooltip getCell() {
                TextCellWithEditableTooltip textCellWithEditableTooltip = super.getCell();
                textCellWithEditableTooltip.setTitle(constants.quotaCalculationsMessage());
                return textCellWithEditableTooltip;
            }
        },
                constants.usedStorageTotalQuotaStorage());
    }
}
