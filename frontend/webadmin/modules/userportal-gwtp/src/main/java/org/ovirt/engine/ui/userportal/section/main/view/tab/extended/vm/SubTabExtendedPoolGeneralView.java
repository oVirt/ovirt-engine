package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.vm.PoolGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedPoolGeneralPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabExtendedPoolGeneralView extends
    AbstractSubTabFormView<UserPortalItemModel, UserPortalListModel, PoolGeneralModel> implements
    SubTabExtendedPoolGeneralPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedPoolGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabExtendedPoolGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @WithElementId
    PoolGeneralModelForm form;

    @Inject
    public SubTabExtendedPoolGeneralView(
            UserPortalDetailModelProvider<UserPortalListModel, PoolGeneralModel> modelProvider) {
        super(modelProvider);
        form = new PoolGeneralModelForm(modelProvider);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        generateIds();

        form.initialize();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(UserPortalItemModel selectedItem) {
        update();
    }

    @Override
    public void update() {
        form.update();
    }

}
