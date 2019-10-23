package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.AffinityLabelListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class AffinityLabelsActionPanelPresenterWidget<E, M extends ListWithDetailsModel,
    D extends AffinityLabelListModel<?>> extends DetailActionPanelPresenterWidget<E, Label, M, D> {

    protected static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public AffinityLabelsActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<E, Label> view,
            SearchableDetailModelProvider<Label, M, D> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<E, Label>(constants.affinityLabelsSubTabNewButton()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<E, Label>(constants.affinityLabelsSubTabEditButton()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
    }

}
