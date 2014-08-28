package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabProviderGeneralView extends AbstractSubTabFormView<Provider, ProviderListModel, ProviderGeneralModel> implements SubTabProviderGeneralPresenter.ViewDef, Editor<ProviderGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabProviderGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabProviderGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends SimpleBeanEditorDriver<ProviderGeneralModel, SubTabProviderGeneralView> { }

    private final Driver driver = GWT.create(Driver.class);
    private final ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    TextBoxLabel name = new TextBoxLabel();
    ValueLabel<ProviderType> type = new ValueLabel<ProviderType>(new EnumRenderer<ProviderType>());
    TextBoxLabel description = new TextBoxLabel();
    TextBoxLabel url = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabProviderGeneralView(DetailModelProvider<ProviderListModel, ProviderGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 4);

        formBuilder.addFormItem(new FormItem(constants.nameProvider(), name, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.typeProvider(), type, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.descriptionProvider(), description, 2, 0));
        formBuilder.addFormItem(new FormItem(constants.urlProvider(), url, 3, 0));
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(Provider selectedItem) {
        driver.edit(getDetailModel());
        formBuilder.update(getDetailModel());
    }

}
