package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.gin.BaseClientGinjector;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.CommonModelChangeEvent.CommonModelChangeHandler;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

/**
 * Basic {@link ModelProvider} implementation that uses {@link CommonModelManager} for accessing the CommonModel
 * instance.
 *
 * @param <M>
 *            Model type.
 */
public abstract class TabModelProvider<M extends EntityModel> implements ModelProvider<M> {

    private final EventBus eventBus;
    private final Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider;

    private AbstractModelBoundPopupPresenterWidget<?, ?> windowPopup;
    private AbstractModelBoundPopupPresenterWidget<?, ?> confirmWindowPopup;

    public TabModelProvider(BaseClientGinjector ginjector) {
        this.eventBus = ginjector.getEventBus();
        this.defaultConfirmPopupProvider = ginjector.getDefaultConfirmationPopupProvider();

        // Add handler to be notified when the CommonModel instance changes
        eventBus.addHandler(CommonModelChangeEvent.getType(), new CommonModelChangeHandler() {
            @Override
            public void onCommonModelChange(CommonModelChangeEvent event) {
                TabModelProvider.this.onCommonModelChange();
            }
        });
    }

    @Override
    public void setEntity(Object value) {
        getModel().setEntity(value);
    }

    protected CommonModel getCommonModel() {
        return CommonModelManager.instance();
    }

    /**
     * Callback fired when the {@link CommonModel} reference changes.
     * <p>
     * Override this method to register custom listeners on the corresponding model.
     */
    protected void onCommonModelChange() {
        // Add necessary property change handlers
        getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;

                // Handle popups that bind to "Window" and "ConfirmWindow" model properties
                if ("Window".equals(propName)) {
                    handleWindowModelChange(windowPopup, false);
                } else if ("ConfirmWindow".equals(propName)) {
                    handleWindowModelChange(confirmWindowPopup, true);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    void handleWindowModelChange(AbstractModelBoundPopupPresenterWidget<?, ?> popup, boolean isConfirm) {
        Model windowModel = isConfirm ? getModel().getConfirmWindow() : getModel().getWindow();

        // Reveal new popup
        if (windowModel != null && popup == null) {
            AbstractModelBoundPopupPresenterWidget<?, ?> newPopup = null;
            UICommand lastExecutedCommand = getModel().getLastExecutedCommand();

            // Resolve
            if (windowModel instanceof ConfirmationModel) {
                newPopup = getConfirmModelPopup(lastExecutedCommand);
            } else {
                newPopup = getModelPopup(lastExecutedCommand);
            }

            // Reveal
            if (newPopup != null) {
                revealAndAssignPopup(windowModel,
                        (AbstractModelBoundPopupPresenterWidget<Model, ?>) newPopup, isConfirm);
            } else {
                // No popup bound to model, need to clear model reference manually
                if (isConfirm) {
                    getModel().setConfirmWindow(null);
                } else {
                    getModel().setWindow(null);
                }
            }
        }

        // Hide existing popup
        else if (windowModel == null && popup != null) {
            hideAndClearPopup(popup, isConfirm);
            forceRefresh(getModel());
        }
    }

    protected AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UICommand lastExecutedCommand) {
        // No-op, override as necessary
        return null;
    }

    protected AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(UICommand lastExecutedCommand) {
        // Reveal basic confirmation popup by default
        return defaultConfirmPopupProvider.get();
    }

    /**
     * Reveals a popup bound to the given model.
     */
    <T extends Model> void revealPopup(final T model,
            final AbstractModelBoundPopupPresenterWidget<T, ?> popup) {
        assert (model != null) : "Popup model must not be null";

        // Initialize popup
        popup.init(model);

        // Add "Progress" property change handler to Window model
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs pcArgs = (PropertyChangedEventArgs) args;

                if ("Progress".equals(pcArgs.PropertyName)) {
                    if (model.getProgress() != null) {
                        popup.startProgress(model.getProgress().getCurrentOperation());
                    } else {
                        popup.stopProgress();
                    }
                }
            }
        });

        // Reveal popup
        RevealRootPopupContentEvent.fire(eventBus, popup);
    }

    <T extends Model> void revealAndAssignPopup(T model,
            AbstractModelBoundPopupPresenterWidget<T, ?> popup, boolean isConfirm) {
        revealPopup(model, popup);

        // Assign popup reference
        if (isConfirm) {
            confirmWindowPopup = popup;
        } else {
            windowPopup = popup;
        }
    }

    void hideAndClearPopup(AbstractModelBoundPopupPresenterWidget<?, ?> popup, boolean isConfirm) {
        popup.hideAndUnbind();

        // Clear popup reference
        if (isConfirm) {
            confirmWindowPopup = null;
        } else {
            windowPopup = null;
        }
    }

    void forceRefresh(M model) {
        if (model instanceof SearchableListModel && !model.getLastExecutedCommand().getIsCancel()) {
            // Refresh the grid after a Dialog
            SearchableListModel searchableList = (SearchableListModel) model;
            searchableList.getForceRefreshCommand().Execute();
        }

    }

}
