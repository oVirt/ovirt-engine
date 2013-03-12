package org.ovirt.engine.ui.common.widget.uicommon.popup.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ChangeQuotaItemView extends Composite implements HasEditorDriver<ChangeQuotaItemModel>, HasElementId {

    interface Driver extends SimpleBeanEditorDriver<ChangeQuotaItemModel, ChangeQuotaItemView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, ChangeQuotaItemView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface WidgetStyle extends CssResource {
        String editorContent();

        String editorWrapper();

        String editorLabel();
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Ignore
    EntityModelLabelEditor objectNameLabel;

    @UiField
    @Ignore
    EntityModelLabelEditor currentQuotaLabel;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    ListModelListBoxEditor<Object> quotaListEditor;

    private final Driver driver = GWT.create(Driver.class);

    private final CommonApplicationConstants constants;

    public ChangeQuotaItemView(CommonApplicationConstants constants) {
        this.constants = constants;

        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
        updateStyles();
    }

    void initEditors() {
        quotaListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
    }

    void updateStyles() {
        String editorStyle = style.editorContent();

        updateEditorStyle(objectNameLabel, editorStyle);
        updateEditorStyle(currentQuotaLabel, editorStyle);
        updateEditorStyle(quotaListEditor, editorStyle);
    }

    private void updateEditorStyle(AbstractValidatedWidgetWithLabel editor, String contentStyle) {
        editor.setContentWidgetStyleName(contentStyle);
        editor.addWrapperStyleName(style.editorWrapper());
        editor.setLabelStyleName(style.editorLabel());
    }

    @Override
    public void edit(final ChangeQuotaItemModel object) {
        driver.edit(object);

        objectNameLabel.asValueBox().setValue(object.getObject().getEntity());
        currentQuotaLabel.asValueBox().setValue(object.getCurrentQuota().getEntity());
    }

    @Override
    public ChangeQuotaItemModel flush() {
        return driver.flush();
    }

    @Override
    public void setElementId(String elementId) {
    }

}
