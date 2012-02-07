package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateGeneralPresenter;
import org.ovirt.engine.ui.webadmin.widget.form.GeneralFormPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabTemplateGeneralView extends AbstractSubTabFormView<VmTemplate, TemplateListModel, TemplateGeneralModel> implements SubTabTemplateGeneralPresenter.ViewDef, Editor<TemplateGeneralModel> {

    interface Driver extends SimpleBeanEditorDriver<TemplateGeneralModel, SubTabTemplateGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabTemplateGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    TextBoxLabel name = new TextBoxLabel();
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel hostCluster = new TextBoxLabel();
    TextBoxLabel definedMemory = new TextBoxLabel();
    TextBoxLabel oS = new TextBoxLabel();
    TextBoxLabel cpuInfo = new TextBoxLabel();
    TextBoxLabel defaultDisplayType = new TextBoxLabel();
    TextBoxLabel origin = new TextBoxLabel();
    TextBoxLabel priority = new TextBoxLabel();
    TextBoxLabel usbPolicy = new TextBoxLabel();
    TextBoxLabel domain = new TextBoxLabel();
    TextBoxLabel timeZone = new TextBoxLabel();

    @Ignore
    TextBoxLabel monitorCount = new TextBoxLabel();
    @Ignore
    TextBoxLabel isHighlyAvailable = new TextBoxLabel();
    @Ignore
    TextBoxLabel isStateless = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabTemplateGeneralView(DetailModelProvider<TemplateListModel, TemplateGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 3, 6);
        formBuilder.setColumnsWidth("120px", "240px", "160px");
        formBuilder.addFormItem(new FormItem("Name", name, 0, 0));
        formBuilder.addFormItem(new FormItem("Description", description, 1, 0));
        formBuilder.addFormItem(new FormItem("Host Cluster", hostCluster, 2, 0));
        formBuilder.addFormItem(new FormItem("Operating System", oS, 3, 0));
        formBuilder.addFormItem(new FormItem("Default Display Type", defaultDisplayType, 4, 0));

        formBuilder.addFormItem(new FormItem("Defined Memory", definedMemory, 0, 1));
        formBuilder.addFormItem(new FormItem("Number of CPU Cores", cpuInfo, 1, 1));
        formBuilder.addFormItem(new FormItem("Number of Monitors", monitorCount, 2, 1));
        formBuilder.addFormItem(new FormItem("Highly Available", isHighlyAvailable, 3, 1));
        formBuilder.addFormItem(new FormItem("Priority", priority, 4, 1));
        formBuilder.addFormItem(new FormItem("USB Policy", usbPolicy, 5, 1) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasUsbPolicy();
            }
        });

        formBuilder.addFormItem(new FormItem("Origin", origin, 0, 2));
        formBuilder.addFormItem(new FormItem("Is Stateless", isStateless, 1, 2));
        formBuilder.addFormItem(new FormItem("Domain", domain, 2, 2) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasDomain();
            }
        });
        formBuilder.addFormItem(new FormItem("Time Zone", timeZone, 3, 2) {
            @Override
            public boolean isVisible() {
                return getDetailModel().getHasTimeZone();
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(VmTemplate selectedItem) {
        Driver.driver.edit(getDetailModel());

        // TODO required because of GWT#5864
        monitorCount.setText(Integer.toString(getDetailModel().getMonitorCount()));
        isHighlyAvailable.setText(Boolean.toString(getDetailModel().getIsHighlyAvailable()));
        isStateless.setText(Boolean.toString(getDetailModel().getIsStateless()));

        formBuilder.showForm(getDetailModel(), Driver.driver);
    }

}
