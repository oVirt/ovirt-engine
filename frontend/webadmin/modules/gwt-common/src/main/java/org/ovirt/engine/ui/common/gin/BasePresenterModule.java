package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.presenter.CollapsiblePanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.ErrorPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.RolePermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.numa.NumaSupportPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.numa.UnassignedVNumaNodesPanelPresenterWidget;
import org.ovirt.engine.ui.common.section.main.presenter.OptionsPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.CollapsiblePanelView;
import org.ovirt.engine.ui.common.view.popup.ConsolePopupView;
import org.ovirt.engine.ui.common.view.popup.DefaultConfirmationPopupView;
import org.ovirt.engine.ui.common.view.popup.ErrorPopupView;
import org.ovirt.engine.ui.common.view.popup.OptionsPopupView;
import org.ovirt.engine.ui.common.view.popup.RemoveConfirmationPopupView;
import org.ovirt.engine.ui.common.view.popup.RolePermissionsRemoveConfirmationPopupView;
import org.ovirt.engine.ui.common.view.popup.numa.NumaSupportPopupView;
import org.ovirt.engine.ui.common.view.popup.numa.UnassignedVNumaNodesPanelView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * GIN module containing common GWTP presenter bindings.
 */
public abstract class BasePresenterModule extends AbstractPresenterModule {

    protected void bindCommonPresenters() {
        // Error popup
        bindPresenterWidget(ErrorPopupPresenterWidget.class,
                ErrorPopupPresenterWidget.ViewDef.class,
                ErrorPopupView.class);

        // Confirmation popups
        bindPresenterWidget(DefaultConfirmationPopupPresenterWidget.class,
                DefaultConfirmationPopupPresenterWidget.ViewDef.class,
                DefaultConfirmationPopupView.class);
        bindPresenterWidget(RemoveConfirmationPopupPresenterWidget.class,
                RemoveConfirmationPopupPresenterWidget.ViewDef.class,
                RemoveConfirmationPopupView.class);
        // Permissions removal
        bindPresenterWidget(RolePermissionsRemoveConfirmationPopupPresenterWidget.class,
                RolePermissionsRemoveConfirmationPopupPresenterWidget.ViewDef.class,
                RolePermissionsRemoveConfirmationPopupView.class);

        // Console popup
        bindPresenterWidget(ConsolePopupPresenterWidget.class,
                ConsolePopupPresenterWidget.ViewDef.class,
                ConsolePopupView.class);
        bindPresenterWidget(CollapsiblePanelPresenterWidget.class,
                CollapsiblePanelPresenterWidget.ViewDef.class,
                CollapsiblePanelView.class);

        // Numa popup.
        bindPresenterWidget(UnassignedVNumaNodesPanelPresenterWidget.class,
                UnassignedVNumaNodesPanelPresenterWidget.ViewDef.class,
                UnassignedVNumaNodesPanelView.class);
        bindPresenterWidget(NumaSupportPopupPresenterWidget.class,
                NumaSupportPopupPresenterWidget.ViewDef.class,
                NumaSupportPopupView.class);

        // Options popups
        bindPresenterWidget(OptionsPopupPresenterWidget.class,
                OptionsPopupPresenterWidget.ViewDef.class,
                OptionsPopupView.class);
    }

}
