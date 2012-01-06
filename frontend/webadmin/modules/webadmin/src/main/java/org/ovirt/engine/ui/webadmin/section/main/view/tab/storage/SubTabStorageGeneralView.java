package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.webadmin.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.form.FormBuilder;
import org.ovirt.engine.ui.webadmin.widget.form.FormItem;
import org.ovirt.engine.ui.webadmin.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.webadmin.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.webadmin.widget.label.PercentLabel;
import org.ovirt.engine.ui.webadmin.widget.label.TextBoxLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabStorageGeneralView extends AbstractSubTabFormView<storage_domains, StorageListModel, StorageGeneralModel> implements SubTabStorageGeneralPresenter.ViewDef, Editor<StorageGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<StorageGeneralModel, SubTabStorageGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabStorageGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> totalSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> availableSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    DiskSizeLabel<Integer> usedSize = new DiskSizeLabel<Integer>();

    @Ignore
    // TODO Primitive getters not supported in 2.2
    PercentLabel<Integer> overAllocationRatio = new PercentLabel<Integer>();

    @Path("nfsPath")
    TextBoxLabel nfsExportPath = new TextBoxLabel();

    @Path("localPath")
    TextBoxLabel hostLocalPath = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabStorageGeneralView(DetailModelProvider<StorageListModel, StorageGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 6);
        formBuilder.setColumnsWidth("100%");
        formBuilder.addFormItem(new FormItem("Size", totalSize, 0, 0));
        formBuilder.addFormItem(new FormItem("Available", availableSize, 1, 0));
        formBuilder.addFormItem(new FormItem("Used", usedSize, 2, 0));
        formBuilder.addFormItem(new FormItem("Over Allocation Ratio", overAllocationRatio, 3, 0) {
            @Override
            public boolean isVisible() {
                StorageDomainType storageDomainType =
                        ((storage_domains) getDetailModel().getEntity()).getstorage_domain_type();
                return !storageDomainType.equals(StorageDomainType.ISO)
                        && !storageDomainType.equals(StorageDomainType.ImportExport);
            }
        });
        formBuilder.addFormItem(new FormItem("", new InlineLabel(""), 4, 0)); // empty cell
        formBuilder.addFormItem(new FormItem("NFS Export Path", nfsExportPath, 5, 0) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getIsNfs();
            }
        });
        formBuilder.addFormItem(new FormItem("Local Path on Host", hostLocalPath, 5, 0) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getIsLocalS();
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(storage_domains selectedItem) {
        Driver.driver.edit(getDetailModel());

        // TODO required because of editor driver errors
        // Possible reasons: lowercase getters, StorageGeneralModel.getEntity() returns Object
        storage_domains entity = (storage_domains) getDetailModel().getEntity();

        if (entity == null)
            return;

        totalSize.setValue(entity.getTotalDiskSize());
        availableSize.setValue(entity.getavailable_disk_size());
        usedSize.setValue(entity.getused_disk_size());
        overAllocationRatio.setValue(entity.getstorage_domain_over_commit_percent());

        formBuilder.showForm(getDetailModel(), Driver.driver);
    }

}
