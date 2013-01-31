package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.GlusterHookContentModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class GlusterHookContentPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterHookContentModel, GlusterHookContentPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterHookContentModel> {
    }

    @Inject
    public GlusterHookContentPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(GlusterHookContentModel model) {
        super.init(model);
    }

}
