package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AdRefStatus;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.EnumLabel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SubTabUserGeneralView extends AbstractSubTabFormView<DbUser, UserListModel, UserGeneralModel> implements SubTabUserGeneralPresenter.ViewDef, Editor<UserGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabUserGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<UserGeneralModel, SubTabUserGeneralView> {
        Driver driver = GWT.create(Driver.class);
    }

    TextBoxLabel domain = new TextBoxLabel();

    EnumLabel<AdRefStatus> status = new EnumLabel<AdRefStatus>();

    TextBoxLabel email = new TextBoxLabel();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    @Inject
    public SubTabUserGeneralView(DetailModelProvider<UserListModel, UserGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 3);
        formBuilder.addFormItem(new FormItem("Domain", domain, 0, 0));
        formBuilder.addFormItem(new FormItem("Status", status, 1, 0));
        formBuilder.addFormItem(new FormItem("E-mail", email, 2, 0) {
            @Override
            public boolean isVisible() {
                return !((DbUser) getDetailModel().getEntity()).getIsGroup();
            }
        });
    }

    @Override
    public void setMainTabSelectedItem(DbUser selectedItem) {
        Driver.driver.edit(getDetailModel());

        formBuilder.showForm(getDetailModel());
    }

}
