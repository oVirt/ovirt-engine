package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.presenter.ErrorPopupPresenterWidget;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * GIN module containing common GWTP presenter bindings.
 */
public abstract class BasePresenterModule extends AbstractPresenterModule {

    protected void bindCommonPresenters(
            Class<? extends ErrorPopupPresenterWidget.ViewDef> errorPopupView) {
        bindSingletonPresenterWidget(ErrorPopupPresenterWidget.class,
                ErrorPopupPresenterWidget.ViewDef.class,
                errorPopupView);
    }

}
