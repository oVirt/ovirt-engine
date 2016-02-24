package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Map;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksBondModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class SetupNetworksBondPopupView extends AbstractModelBoundPopupView<SetupNetworksBondModel> implements SetupNetworksBondPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<SetupNetworksBondModel, SetupNetworksBondPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, SetupNetworksBondPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path(value = "bond.selectedItem")
    ListModelSuggestBoxEditor bondSuggestEditor;

    @UiField(provided = true)
    @Path(value = "bondingOptions.selectedItem")
    ListModelListBoxEditor<Map.Entry<String, EntityModel<String>>> bondingModeEditor;

    @UiField
    @Path(value = "customBondEditor.entity")
    StringEntityModelTextBoxEditor customBondEditor;

    @UiField
    @Ignore
    VerticalPanel mainPanel;

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
                    return constants.customHostPopup() + ": " + value; //$NON-NLS-1$
                }
                return value;
            }
        });

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        // Set Styles
        mainPanel.getElement().setPropertyString("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        // Localize
        bondSuggestEditor.setLabel(constants.bondNameHostPopup() + ":"); //$NON-NLS-1$
        bondingModeEditor.setLabel(constants.bondingModeHostPopup() + ":"); //$NON-NLS-1$
        customBondEditor.setLabel(constants.customModeHostPopup() + ":"); //$NON-NLS-1$

        driver.initialize(this);
    }

    @Override
    public void edit(final SetupNetworksBondModel object) {
        driver.edit(object);

        updateBondOptions(object.getBondingOptions());

        object.getBondingOptions().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ListModel<Map.Entry<String, EntityModel<String>>> list = (ListModel<Map.Entry<String, EntityModel<String>>>) sender;
                updateBondOptions(list);
            }
        });
    }

    @Override
    public SetupNetworksBondModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        bondSuggestEditor.setFocus(true);
    }

    private void updateBondOptions(ListModel<Map.Entry<String, EntityModel<String>>> list) {
        Map.Entry<String, EntityModel<String>> pair = list.getSelectedItem();
        if ("custom".equals(pair.getKey())) { //$NON-NLS-1$
            customBondEditor.setVisible(true);
            String entity = pair.getValue().getEntity();
            customBondEditor.asEditor().getSubEditor().setValue(entity == null ? "" : entity); //$NON-NLS-1$
        } else {
            customBondEditor.setVisible(false);
        }
    }
}
