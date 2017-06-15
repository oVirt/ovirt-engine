package org.ovirt.engine.ui.common.widget.uicommon;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for widgets that use {@link GeneralFormPanel} to represent UiCommon entity models.
 *
 * @param <T>
 *            Model type being edited.
 */
public abstract class AbstractModelBoundFormWidget<T extends EntityModel> extends Composite implements Editor<T>, HasElementId, HasCleanup {

    private final ModelProvider<T> modelProvider;
    private final GeneralFormPanel formPanel;

    protected final FormBuilder formBuilder;

    public AbstractModelBoundFormWidget(ModelProvider<T> modelProvider, int numOfColumns, int numOfRows) {
        this.modelProvider = modelProvider;
        this.formPanel = new GeneralFormPanel();
        this.formBuilder = new FormBuilder(formPanel, numOfColumns, numOfRows);
        initWidget(formPanel);
    }

    protected T getModel() {
        return modelProvider.getModel();
    }

    /**
     * Updates this Editor widget according to the current model.
     */
    public void update() {
        T model = getModel();

        doEdit(model);
        formBuilder.update(model);
    }

    /**
     * Performs the actual Editor logic, using Editor Driver to update fields of the widget.
     */
    protected abstract void doEdit(T model);

    public void setElementId(String elementId) {
        this.getElement().setId(elementId);
        formPanel.setElementId(elementId);
    }

}
