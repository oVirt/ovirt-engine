package org.ovirt.engine.ui.webadmin.section.main.view.tab.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.DiskSizeLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.label.PercentLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;


public class SubTabStorageGeneralView extends AbstractSubTabFormView<StorageDomain, StorageListModel, StorageGeneralModel> implements SubTabStorageGeneralPresenter.ViewDef, Editor<StorageGeneralModel> {

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
    public SubTabStorageGeneralView(DetailModelProvider<StorageListModel, StorageGeneralModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 6);
        formBuilder.setColumnsWidth("100%"); //$NON-NLS-1$
        formBuilder.addFormItem(new FormItem(constants.sizeStorageGeneral(), totalSize, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.availableStorageGeneral(), availableSize, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.usedStorageGeneral(), usedSize, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.overAllocRatioStorageGeneral(), overAllocationRatio, 3, 0) {
            @Override
            public boolean isVisible() {
                StorageDomainType storageDomainType =
                        ((StorageDomain) getDetailModel().getEntity()).getstorage_domain_type();
                return !storageDomainType.equals(StorageDomainType.ISO)
                        && !storageDomainType.equals(StorageDomainType.ImportExport);
            }
        });
        formBuilder.addFormItem(new FormItem("", new InlineLabel(""), 4, 0)); // empty cell //$NON-NLS-1$ $NON-NLS-2$
        formBuilder.addFormItem(new FormItem(constants.nfsExportPathStorageGeneral(), nfsExportPath, 5, 0) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getIsNfs();
            }
        });
        formBuilder.addFormItem(new FormItem(constants.localPathOnHostStorageGeneral(), hostLocalPath, 5, 0) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getIsLocalS();
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(StorageDomain selectedItem) {
        Driver.driver.edit(getDetailModel());

        // TODO required because of editor driver errors
        // Possible reasons: lowercase getters, StorageGeneralModel.getEntity() returns Object
        StorageDomain entity = (StorageDomain) getDetailModel().getEntity();

        if (entity == null)
            return;

        totalSize.setValue(entity.getTotalDiskSize());
        availableSize.setValue(entity.getavailable_disk_size());
        usedSize.setValue(entity.getused_disk_size());
        overAllocationRatio.setValue(entity.getstorage_domain_over_commit_percent());

        formBuilder.showForm(getDetailModel());
    }

}
