package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralSoftwarePresenter;
import org.ovirt.engine.ui.webadmin.widget.label.VersionTextBoxLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabHostGeneralSoftwareView extends AbstractSubTabFormView<VDS, HostListModel<Void>, HostGeneralModel>
    implements SubTabHostGeneralSoftwarePresenter.ViewDef, Editor<HostGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<HostGeneralModel, SubTabHostGeneralSoftwareView> {
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabHostGeneralSoftwareView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabHostGeneralSoftwareView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final ApplicationConstants constants = GWT.create(ApplicationConstants.class);

    private final Driver driver = GWT.create(Driver.class);

    FormBuilder formBuilder;

    @Path("OS")
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel kvmVersion = new TextBoxLabel();
    VersionTextBoxLabel libvirtVersion = new VersionTextBoxLabel();
    TextBoxLabel spiceVersion = new TextBoxLabel();
    TextBoxLabel kernelVersion = new TextBoxLabel();
    VersionTextBoxLabel glusterVersion = new VersionTextBoxLabel();
    VersionTextBoxLabel vdsmVersion = new VersionTextBoxLabel();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    @Inject
    public SubTabHostGeneralSoftwareView(DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider) {
        super(modelProvider);

        // Init form panel:
        formPanel = new GeneralFormPanel();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        buildForm();
    }

    private void buildForm() {
        generateIds();

        boolean virtSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly);
        boolean glusterSupported = ApplicationModeHelper.isModeSupported(ApplicationMode.GlusterOnly);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 7);
        formBuilder.setRelativeColumnWidth(0, 3);
        formBuilder.addFormItem(new FormItem(constants.osVersionHostGeneral(), oS, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.kernelVersionHostGeneral(), kernelVersion, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.kvmVersionHostGeneral(), kvmVersion, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.libvirtVersionHostGeneral(), libvirtVersion, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.vdsmVersionHostGeneral(), vdsmVersion, 0).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.spiceVersionHostGeneral(), spiceVersion, 0, virtSupported).withAutoPlacement());
        formBuilder.addFormItem(new FormItem(constants.glusterVersionHostGeneral(), glusterVersion, 0, glusterSupported).withAutoPlacement());
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        driver.edit(getDetailModel());
        formBuilder.update(getDetailModel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
