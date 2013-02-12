package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public abstract class AbstractSanStorageView extends AbstractStorageView<SanStorageModelBase> implements HasValidation {

    interface Driver extends SimpleBeanEditorDriver<SanStorageModelBase, AbstractSanStorageView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, AbstractSanStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    ScrollPanel listPanel;

    @UiField
    FlowPanel contentPanel;

    @UiField
    FlowPanel extraContentPanel;

    @UiField
    @Ignore
    Label listLabel;

    protected static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    @Inject
    public AbstractSanStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles();
        Driver.driver.initialize(this);
    }

    public AbstractSanStorageView(boolean multiSelection) {
        this();
        this.multiSelection = multiSelection;
    }

    void addStyles() {
    }

    void localize(CommonApplicationConstants constants) {
    }

    protected abstract void initLists(SanStorageModelBase object);

    @Override
    public void edit(final SanStorageModelBase object) {
        Driver.driver.edit(object);

        initLists(object);

        // Add event handlers
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (propName.equals("IsValid")) { //$NON-NLS-1$
                    onIsValidPropertyChange(object);
                }
            }
        });
    }

    void onIsValidPropertyChange(EntityModel model) {
        if (model.getIsValid()) {
            markAsValid();
        } else {
            markAsInvalid(model.getInvalidityReasons());
        }
    }

    @Override
    public void markAsValid() {
        markValidation(false, null);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        markValidation(true, validationHints);
    }

    private void markValidation(boolean isValid, List<String> validationHints) {
        String oldStyle = isValid ? style.validContentPanel() : style.invalidContentPanel();
        String newStyle = isValid ? style.invalidContentPanel() : style.validContentPanel();

        contentPanel.removeStyleName(oldStyle);
        contentPanel.addStyleName(newStyle);

        contentPanel.setTitle(getValidationTitle(validationHints));
    }

    private String getValidationTitle(List<String> validationHints) {
        return validationHints != null && validationHints.size() > 0 ? validationHints.get(0) : null;
    }

    @Override
    public SanStorageModelBase flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
    }

    interface WidgetStyle extends CssResource {
        String validContentPanel();

        String invalidContentPanel();

        String listPanel();

        String treePanel();
    }
}
