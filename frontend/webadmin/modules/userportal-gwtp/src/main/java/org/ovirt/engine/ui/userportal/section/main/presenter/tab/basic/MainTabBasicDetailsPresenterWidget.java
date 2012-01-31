package org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainTabBasicDetailsPresenterWidget extends PresenterWidget<MainTabBasicDetailsPresenterWidget.ViewDef> {

    public interface ViewDef extends View {
    }

    @Inject
    public MainTabBasicDetailsPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
