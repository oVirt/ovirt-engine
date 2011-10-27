package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.StorageDomainStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabTemplateStorageView extends AbstractSubTabTableView<VmTemplate, storage_domains, TemplateListModel, TemplateStorageListModel>
        implements SubTabTemplateStoragePresenter.ViewDef {

    @Inject
    public SubTabTemplateStorageView(SearchableDetailModelProvider<storage_domains, TemplateListModel, TemplateStorageListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new StorageDomainStatusColumn(), "", "30px");

        TextColumn<storage_domains> nameColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getstorage_name();
            }
        };
        getTable().addColumn(nameColumn, "Domain Name");

        TextColumn<storage_domains> typeColumn = new EnumColumn<storage_domains, StorageDomainType>() {
            @Override
            protected StorageDomainType getRawValue(storage_domains object) {
                return object.getstorage_domain_type();
            }
        };
        getTable().addColumn(typeColumn, "Domain Type");

        TextColumn<storage_domains> statusColumn = new EnumColumn<storage_domains, StorageDomainStatus>() {
            @Override
            protected StorageDomainStatus getRawValue(storage_domains object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<storage_domains> freeColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return (object.getTotalDiskSize() - object.getused_disk_size()) + " GB";
            }
        };
        getTable().addColumn(freeColumn, "Free Space");

        TextColumn<storage_domains> usedColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getused_disk_size() + " GB";
            }
        };
        getTable().addColumn(usedColumn, "Used Space");

        TextColumn<storage_domains> totalColumn = new TextColumn<storage_domains>() {
            @Override
            public String getValue(storage_domains object) {
                return object.getTotalDiskSize() + " GB";
            }
        };
        getTable().addColumn(totalColumn, "Total Space");
    }

}
