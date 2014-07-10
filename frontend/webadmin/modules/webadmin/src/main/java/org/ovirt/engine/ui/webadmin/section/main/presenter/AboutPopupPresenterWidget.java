package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the WebAdmin about dialog.
 */
public class AboutPopupPresenterWidget extends AbstractPopupPresenterWidget<AboutPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {

        void setVersion(String version);

    }

    @Inject
    public AboutPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String version = (String) result;

                getView().setVersion(version);
            }
        };

        AsyncDataProvider.getInstance().getRpmVersion(_asyncQuery);
    }

}
