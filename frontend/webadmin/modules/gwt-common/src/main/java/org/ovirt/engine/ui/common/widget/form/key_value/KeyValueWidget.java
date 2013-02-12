package org.ovirt.engine.ui.common.widget.form.key_value;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueWidget extends Composite implements HasEditorDriver<KeyValueModel>, IndexedPanel {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<KeyValueModel, KeyValueWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    @UiField
    VerticalPanel panel;

    private ArrayList<KeyValueLineWidget> widgetList = new ArrayList<KeyValueLineWidget>();
    private boolean enabled = true;

    KeyValueWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final KeyValueModel object) {
        setPanel(object);
        if (object.getKeyValueLines().getItemsChangedEvent().getListeners().size() == 0) {
            object.getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

                @Override
                public void eventRaised(Event ev, Object sender, EventArgs args) {
                    setPanel(object);
                }
            });
        }
    }

    private void setPanel(KeyValueModel object) {
        List<KeyValueLineModel> list = (List<KeyValueLineModel>) object.getKeyValueLines().getItems();
        panel.clear();
        widgetList = new ArrayList<KeyValueLineWidget>();
        //create & edit each row.
        for (KeyValueLineModel keyValueLineModel : list) {
            KeyValueLineWidget keyValueLineWidget = new KeyValueLineWidget();
            widgetList.add(keyValueLineWidget);
            panel.add(keyValueLineWidget);
            keyValueLineWidget.edit(keyValueLineModel);
            keyValueLineWidget.setEnabled(enabled);
        }
    }

    @Override
    public KeyValueModel flush() {
        for (KeyValueLineWidget lineWidget : widgetList) {
            lineWidget.flush();
        }
        return Driver.driver.flush();
    }

    @Override
    public Widget getWidget(int index) {
        return widgetList.get(index);
    }

    @Override
    public int getWidgetCount() {
        return widgetList.size();
    }

    /**
     * currently not needed
     */
    @Override
    public int getWidgetIndex(Widget child) {
        throw new NotImplementedException();
    }

    /**
     * currently not needed
     */
    @Override
    public boolean remove(int index) {
        throw new NotImplementedException();
    }

    public void setEnabled(boolean value) {
        enabled = value;
        for (KeyValueLineWidget widget : widgetList) {
            widget.setEnabled(enabled);
        }
    }
}
