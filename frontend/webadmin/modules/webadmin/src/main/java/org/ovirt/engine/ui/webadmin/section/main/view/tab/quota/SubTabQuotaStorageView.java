package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import java.util.Comparator;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.table.cell.TextCellWithEditableTooltip;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithEditableTooltip;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaStorageView extends AbstractSubTabTableView<Quota, QuotaStorage, QuotaListModel, QuotaStorageListModel>
        implements SubTabQuotaStoragePresenter.ViewDef {

    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<Number>(SizeConverter.SizeUnit.GB);

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaStorageView(SearchableDetailModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel> modelProvider,
            ApplicationConstants constants, ApplicationMessages messages) {
        super(modelProvider);
        initTable(constants, messages);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable(final ApplicationConstants constants, final ApplicationMessages messages) {
        getTable().enableColumnResizing();

        AbstractTextColumnWithTooltip<QuotaStorage> nameColumn = new AbstractTextColumnWithTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                return object.getStorageName() == null || object.getStorageName().equals("") ? constants.utlQuotaAllStoragesQuotaPopup()
                        : object.getStorageName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameQuotaStorage(), "400px"); //$NON-NLS-1$

        AbstractTextColumnWithEditableTooltip<QuotaStorage> usedColumn = new AbstractTextColumnWithEditableTooltip<QuotaStorage>() {
            @Override
            public String getValue(QuotaStorage object) {
                if (object.getStorageSizeGB() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getStorageSizeGB().equals(QuotaStorage.UNLIMITED)) {
                    return messages.unlimitedStorageConsumption(object.getStorageSizeGBUsage() == 0 ?
                            "0" : //$NON-NLS-1$
                            diskSizeRenderer.render(object.getStorageSizeGBUsage()));
                } else {
                    return messages.limitedStorageConsumption(object.getStorageSizeGBUsage() == 0 ?
                            "0" : //$NON-NLS-1$
                            diskSizeRenderer.render(object.getStorageSizeGBUsage())
                            , object.getStorageSizeGB());
                }
            }

            @Override
            public TextCellWithEditableTooltip getCell() {
                TextCellWithEditableTooltip textCellWithEditableTooltip = super.getCell();
                textCellWithEditableTooltip.setTitle(constants.quotaCalculationsMessage());
                return textCellWithEditableTooltip;
            }
        };
        usedColumn.makeSortable(new Comparator<QuotaStorage>() {
            @Override
            public int compare(QuotaStorage quotaStorage1, QuotaStorage quotaStorage2) {
                return quotaStorage1.getStorageSizeGBUsage().compareTo(quotaStorage2.getStorageSizeGBUsage());
            }
        });
        getTable().addColumn(usedColumn, constants.usedStorageTotalQuotaStorage(), "400px"); //$NON-NLS-1$
    }
}
