package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.HasCleanup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.common.ProgressModel;
import org.ovirt.engine.ui.uicompat.IProvidePropertyChangedEvent;

/**
 * Common interface implemented by all Model objects via the {@link org.ovirt.engine.ui.uicommonweb.models.Model} base class.
 * It's purpose for existence is purely to restrict the {@link org.ovirt.engine.ui.uicommonweb.models.HasEntity}
 * interface to subclasses of {@link org.ovirt.engine.ui.uicommonweb.models.Model}.
 */
public interface IModel extends IProvidePropertyChangedEvent, HasCleanup {

    /**
     * Get "Window" property value.
     */
    Model getWindow();

    /**
     * Set "Window" property value.
     */
    void setWindow(Model value);

    /**
     * Map "Window"-like (generic dialog trigger) property names to their <em>current</em> values.
     * Resulting map's key set should be immutable (keys not expected to change over time).
     * <p>
     * Default implementation should return a map containing single key, "Window".
     *
     * @see #getWindow()
     */
    Map<String, Model> getWindowProperties();

    /**
     * Set value of the given "Window"-like property.
     * <p>
     * Keep this method in sync with {@link #getWindowProperties}.
     *
     * @see #setWindow(Model)
     */
    void setWindowProperty(String propName, Model value);

    /**
     * Get "ConfirmWindow" property value.
     */
    Model getConfirmWindow();

    /**
     * Set "ConfirmWindow" property value.
     */
    void setConfirmWindow(Model value);

    /**
     * Map "ConfirmWindow"-like (confirmation dialog trigger) property names to their <em>current</em> values.
     * Resulting map's key set should be immutable (keys not expected to change over time).
     * <p>
     * Default implementation should return a map containing single key, "ConfirmWindow".
     *
     * @see #getConfirmWindow()
     */
    Map<String, Model> getConfirmWindowProperties();

    /**
     * Set value of the given "ConfirmWindow"-like property.
     * <p>
     * Keep this method in sync with {@link #getConfirmWindowProperties}.
     *
     * @see #setConfirmWindow(Model)
     */
    void setConfirmWindowProperty(String propName, Model value);

    Model getWidgetModel();

    UICommand getLastExecutedCommand();

    ProgressModel getProgress();

    String getHashName();

    boolean getIsAvailable();

    IModel setIsChangeable(boolean value);

}
