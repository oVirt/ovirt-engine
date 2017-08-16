package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageLeaseListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageLeasePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;

public class SubTabStorageLeaseView extends AbstractSubTabTableView<StorageDomain, VmBase, StorageListModel, StorageLeaseListModel>
        implements SubTabStorageLeasePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabStorageLeaseView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationResources resources = AssetProvider.getResources();

    private AbstractTextColumn<VmBase> aliasColumn;
    private AbstractColumn<VmBase, VmBase> typeColumn;

    @Inject
    public SubTabStorageLeaseView(SearchableDetailModelProvider<VmBase, StorageListModel, StorageLeaseListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
        initTableColumns();
    }

    @Override
    public void setMainSelectedItem(StorageDomain storageDomain) {
        initTable(storageDomain);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(StorageDomain storageDomain) {
        if (storageDomain == null) {
            return;
        }
        getTable().enableColumnResizing();

        getTable().ensureColumnVisible(typeColumn, constants.empty(), true, "30px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(aliasColumn, constants.aliasDisk(), true, "120px"); //$NON-NLS-1$
    }

    void initTableColumns() {
        getTable().enableColumnResizing();

        typeColumn = new AbstractColumn<VmBase, VmBase>(new VmTypeCell()) {
            @Override
            public VmBase getValue(VmBase vmBase) {
                return vmBase;
            }

            @Override
            public SafeHtml getTooltip(VmBase vmBase) {
                return SafeHtmlUtils.fromString(vmBase instanceof VmStatic ? constants.vm() : constants.template());
            }
        };

        aliasColumn = new AbstractTextColumn<VmBase>() {
            @Override
            public String getValue(VmBase vmBase) {
                return vmBase.getName();
            }
        };
    }

    private class VmTypeCell extends AbstractCell<VmBase> {
        @Override
        public void render(Context context, VmBase vmBase, SafeHtmlBuilder sb, String id) {
            ImageResource image = vmBase instanceof VmStatic ? resources.vmImage() : resources.templatesImage();
            sb.append(SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(image).getHTML()));
        }
    }
}
