package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class AssignTagsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<TagListModel, AssignTagsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<TagListModel> {
    }

    @Inject
    public AssignTagsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
