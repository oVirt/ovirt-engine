package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class UploadImagePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<UploadImageModel, UploadImagePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UploadImageModel> {
        HasUiCommandClickHandlers getTestButton();
        void updateTestResult(boolean succeeded);
        void showTestCommand(boolean show);
        String getProxyLocation();
    }

    @Inject
    public UploadImagePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final UploadImageModel model) {
        super.init(model);

        model.setProxyLocation(getView().getProxyLocation());

        // Add click handler for the test connection button
        registerHandler(getView().getTestButton().addClickHandler(event -> model.getTestCommand().execute()));

        // Add listener for button click to handle response
        model.getTestResponse().getEntityChangedEvent().addListener((ev, sender, args) -> {
            Response testResponse = model.getTestResponse().getEntity();
            boolean testSucceeded = testResponse != null && testResponse.getStatusCode() == Response.SC_OK;
            getView().updateTestResult(testSucceeded);
        });
    }
}
