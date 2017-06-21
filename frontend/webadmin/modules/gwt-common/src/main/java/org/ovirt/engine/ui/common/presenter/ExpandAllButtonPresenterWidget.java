package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.widget.HasExpandAll;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ExpandAllButtonPresenterWidget extends PresenterWidget<ExpandAllButtonPresenterWidget.ViewDef> {

    public interface ViewDef extends View {
        HasClickHandlers getButton();
        void switchToExpandAll();
        void switchToCollapseAll();
    }

    private HasExpandAll expandAll;
    private boolean expanded;

    @Inject
    public ExpandAllButtonPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    public void setTarget(HasExpandAll expandAll) {
        this.expandAll = expandAll;
    }

    @Override
    public void onBind() {
        super.onBind();
        registerHandler(getView().getButton().addClickHandler(event -> {
            if (expanded) {
                expandAll.collapseAll();
                getView().switchToExpandAll();
            } else {
                expandAll.expandAll();
                getView().switchToCollapseAll();
            }
            expanded = !expanded;
        }));
    }
}
