package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.common.ProgressModel;
import org.ovirt.engine.ui.uicompat.IProvidePropertyChangedEvent;

/**
 * Common interface implemented by all Model objects via the {@link org.ovirt.engine.ui.uicommonweb.models.Model} base class.
 * It's purpose for existence is purely to restrict the {@link org.ovirt.engine.ui.uicommonweb.models.HasEntity}
 * interface to subclasses of {@link org.ovirt.engine.ui.uicommonweb.models.Model}.
 */
public interface IModel extends IProvidePropertyChangedEvent {

    void setWindow(Model value);

    Model getWindow();

    Model getConfirmWindow();

    void setConfirmWindow(Model value);

    Model getWidgetModel();

    UICommand getLastExecutedCommand();

    ProgressModel getProgress();

    String getHashName();

    boolean getIsAvailable();

    IModel setIsChangeable(boolean value);
}
