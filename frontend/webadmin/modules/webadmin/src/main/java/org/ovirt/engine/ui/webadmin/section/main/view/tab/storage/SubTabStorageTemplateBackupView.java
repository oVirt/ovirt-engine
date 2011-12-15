package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabStorageTemplateBackupView extends AbstractSubTabTableView<storage_domains, Entry<VmTemplate, ArrayList<DiskImage>>, StorageListModel, TemplateBackupModel>
        implements SubTabStorageTemplateBackupPresenter.ViewDef {

    @Inject
    public SubTabStorageTemplateBackupView(SearchableDetailModelProvider<Entry<VmTemplate, ArrayList<DiskImage>>, StorageListModel, TemplateBackupModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>> nameColumn =
                new TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>>() {
                    @Override
                    public String getValue(Entry<VmTemplate, ArrayList<DiskImage>> object) {
                        return object.getKey().getname();
                    }
                };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<Entry<VmTemplate, ArrayList<DiskImage>>> originColumn =
                new EnumColumn<Entry<VmTemplate, ArrayList<DiskImage>>, OriginType>() {
                    @Override
                    protected OriginType getRawValue(Entry<VmTemplate, ArrayList<DiskImage>> object) {
                        return object.getKey().getorigin();
                    }
                };
        getTable().addColumn(originColumn, "Origin");

        TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>> memoryColumn =
                new TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>>() {
                    @Override
                    public String getValue(Entry<VmTemplate, ArrayList<DiskImage>> object) {
                        return String.valueOf(object.getKey().getmem_size_mb()) + " MB";
                    }
                };
        getTable().addColumn(memoryColumn, "Memory");

        TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>> cpuColumn =
                new TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>>() {
                    @Override
                    public String getValue(Entry<VmTemplate, ArrayList<DiskImage>> object) {
                        return String.valueOf(object.getKey().getnum_of_cpus());
                    }
                };
        getTable().addColumn(cpuColumn, "CPUs");

        TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>> diskColumn =
                new TextColumnWithTooltip<Entry<VmTemplate, ArrayList<DiskImage>>>() {
                    @Override
                    public String getValue(Entry<VmTemplate, ArrayList<DiskImage>> object) {
                        return String.valueOf(object.getKey().getDiskMap().size());
                    }
                };
        getTable().addColumn(diskColumn, "Disks");

        TextColumn<Entry<VmTemplate, ArrayList<DiskImage>>> creationDateColumn =
                new GeneralDateTimeColumn<Entry<VmTemplate, ArrayList<DiskImage>>>() {
                    @Override
                    protected Date getRawValue(Entry<VmTemplate, ArrayList<DiskImage>> object) {
                        return object.getKey().getcreation_date();
                    }
                };
        getTable().addColumn(creationDateColumn, "Creation Date");
    }

}
