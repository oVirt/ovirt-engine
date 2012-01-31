package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainTabBasicListItemPresenterWidget extends PresenterWidget<MainTabBasicListItemPresenterWidget.ViewDef> {

    public interface ViewDef extends View, HasEditorDriver<UserPortalItemModel> {
    }

    @Inject
    public MainTabBasicListItemPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    public void setModel(UserPortalItemModel model) {
        getView().edit(model);
    }

}
