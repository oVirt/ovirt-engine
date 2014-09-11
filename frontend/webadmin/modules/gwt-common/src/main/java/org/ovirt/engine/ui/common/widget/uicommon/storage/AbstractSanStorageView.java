package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.HasValidation;
import org.ovirt.engine.ui.common.widget.ValidatedPanelWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public abstract class AbstractSanStorageView extends AbstractStorageView<SanStorageModelBase> implements HasValidation {

    interface Driver extends SimpleBeanEditorDriver<SanStorageModelBase, AbstractSanStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, AbstractSanStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    ValidatedPanelWidget contentPanel;

    protected static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public AbstractSanStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles();
        driver.initialize(this);
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
        driver.edit(object);

        initLists(object);

        // Add event handlers
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
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
        contentPanel.markAsValid();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        contentPanel.markAsInvalid(validationHints);
    }

    @Override
    public boolean isValid() {
        return contentPanel.isValid();
    }

    @Override
    public SanStorageModelBase flush() {
        return driver.flush();
    }

    @Override
    public void focus() {
    }

}
