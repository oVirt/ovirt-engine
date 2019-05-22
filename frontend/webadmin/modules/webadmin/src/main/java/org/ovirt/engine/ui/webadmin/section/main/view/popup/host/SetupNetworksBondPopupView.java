package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksBondModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class SetupNetworksBondPopupView extends AbstractModelBoundPopupView<SetupNetworksBondModel> implements SetupNetworksBondPopupPresenterWidget.ViewDef {

    private static final String HAS_WARNING = "has-warning"; //$NON-NLS-1$

    interface Driver extends UiCommonEditorDriver<SetupNetworksBondModel, SetupNetworksBondPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, SetupNetworksBondPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path(value = "bond.selectedItem")
    ListModelSuggestBoxEditor bondSuggestEditor;

    @UiField
    @Ignore
    public Label nameWarningLabel;

    @UiField(provided = true)
    @Path(value = "bondingOptions.selectedItem")
    ListModelListBoxEditor<Map.Entry<String, EntityModel<String>>> bondingModeEditor;

    @UiField
    @Path(value = "customBondEditor.entity")
    StringEntityModelTextBoxEditor customBondEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SetupNetworksBondPopupView(EventBus eventBus) {
        super(eventBus);

        bondSuggestEditor = new ListModelSuggestBoxEditor();
        bondingModeEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<Map.Entry<String, EntityModel<String>>>() {
            @Override
            protected String renderNullSafe(Map.Entry<String, EntityModel<String>> pair) {
                String key = pair.getKey();
                String value = pair.getValue().getEntity();
                if ("custom".equals(key)) { //$NON-NLS-1$
                    return constants.customHostPopup() + " " + value; //$NON-NLS-1$
                }
                return value;
            }
        });

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        nameWarningLabel.setVisible(false);
    }

    @Override
    public void edit(final SetupNetworksBondModel object) {
        object.getBond().getPropertyChangedEvent().addListener((ev, sender, arg) -> {
            SortedListModel<String> model = (SortedListModel<String>) sender;
            int length = model.getSelectedItem().length();
            if (length > BusinessEntitiesDefinitions.BOND_NAME_WARNING_LENGTH
                    && length <= BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH) {
                bondSuggestEditor.addStyleName(HAS_WARNING);
                nameWarningLabel.setVisible(true);
            } else {
                bondSuggestEditor.removeStyleName(HAS_WARNING);
                nameWarningLabel.setVisible(false);
            }
        });
        driver.edit(object);
    }

    @Override
    public SetupNetworksBondModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        bondSuggestEditor.setFocus(true);
    }
}
