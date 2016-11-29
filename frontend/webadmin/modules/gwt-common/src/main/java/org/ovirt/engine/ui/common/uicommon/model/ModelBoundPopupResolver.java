package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

/**
 * Interface for implementing "dialog model &rarr; popup widget" resolution logic.
 *
 * @param <M>
 *            Model type.
 */
public interface ModelBoundPopupResolver<M extends IModel> {

    /**
     * Resolves main popup by last executed command.
     *
     * @param source
     *            Source model that initiated the dialog.
     * @param lastExecutedCommand
     *            Source model's last executed command.
     * @param windowModel
     *            Dialog model.
     * @return Popup presenter widget bound to the dialog model.
     */
    AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(M source,
            UICommand lastExecutedCommand, Model windowModel);

    /**
     * Resolves confirmation popup by last executed command.
     *
     * @param source
     *            Source model that initiated the dialog.
     * @param lastExecutedCommand
     *            Source model's last executed command.
     * @return Popup presenter widget bound to the dialog model.
     */
    AbstractModelBoundPopupPresenterWidget<? extends ConfirmationModel, ?> getConfirmModelPopup(M source,
            UICommand lastExecutedCommand);

}
