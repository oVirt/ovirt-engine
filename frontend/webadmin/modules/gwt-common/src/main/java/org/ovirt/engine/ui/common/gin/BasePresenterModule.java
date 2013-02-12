package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.presenter.popup.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.ErrorPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.popup.ConsolePopupView;
import org.ovirt.engine.ui.common.view.popup.DefaultConfirmationPopupView;
import org.ovirt.engine.ui.common.view.popup.ErrorPopupView;
import org.ovirt.engine.ui.common.view.popup.RemoveConfirmationPopupView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * GIN module containing common GWTP presenter bindings.
 */
public abstract class BasePresenterModule extends AbstractPresenterModule {

    protected void bindCommonPresenters() {
        // Error popup
        bindSingletonPresenterWidget(ErrorPopupPresenterWidget.class,
                ErrorPopupPresenterWidget.ViewDef.class,
                ErrorPopupView.class);

        // Confirmation popups
        bindPresenterWidget(DefaultConfirmationPopupPresenterWidget.class,
                DefaultConfirmationPopupPresenterWidget.ViewDef.class,
                DefaultConfirmationPopupView.class);
        bindPresenterWidget(RemoveConfirmationPopupPresenterWidget.class,
                RemoveConfirmationPopupPresenterWidget.ViewDef.class,
                RemoveConfirmationPopupView.class);

        // Console popup
        bindPresenterWidget(ConsolePopupPresenterWidget.class,
                ConsolePopupPresenterWidget.ViewDef.class,
                ConsolePopupView.class);

    }

}
