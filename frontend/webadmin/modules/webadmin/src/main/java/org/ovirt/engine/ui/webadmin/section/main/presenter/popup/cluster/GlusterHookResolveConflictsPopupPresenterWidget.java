package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.clusters.GlusterHookResolveConflictsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class GlusterHookResolveConflictsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GlusterHookResolveConflictsModel, GlusterHookResolveConflictsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GlusterHookResolveConflictsModel> {
    }

    @Inject
    public GlusterHookResolveConflictsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(GlusterHookResolveConflictsModel model) {
        super.init(model);
    }

}
