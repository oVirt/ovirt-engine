package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TagPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<TagModel, TagPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<TagModel> {
    }

    @Inject
    public TagPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
