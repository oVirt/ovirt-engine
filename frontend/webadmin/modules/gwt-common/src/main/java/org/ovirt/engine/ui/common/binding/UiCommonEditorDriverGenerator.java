package org.ovirt.engine.ui.common.binding;

import org.ovirt.engine.ui.common.editor.AbstractUiCommonModelEditorDriver;
import org.ovirt.engine.ui.common.editor.UiCommonEventMap;
import org.ovirt.engine.ui.common.editor.UiCommonListenerMap;
import org.ovirt.engine.ui.common.editor.UiCommonModelEditorDelegate;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.rebind.AbstractEditorDriverGenerator;
import com.google.gwt.editor.rebind.model.EditorData;
import com.google.gwt.editor.rebind.model.EditorModel;
import com.google.gwt.user.rebind.SourceWriter;

public class UiCommonEditorDriverGenerator extends
        AbstractEditorDriverGenerator {

    private JClassType entityModelType;
    private JClassType listModelType;
    private TreeLogger logger;
    private EditorModel model;
    private SourceWriter sw;

    @Override
    protected Class<?> getDriverInterfaceType() {
        return SimpleBeanEditorDriver.class;
    }

    @Override
    protected Class<?> getDriverSuperclassType() {
        return AbstractUiCommonModelEditorDriver.class;
    }

    @Override
    protected Class<?> getEditorDelegateType() {
        return UiCommonModelEditorDelegate.class;
    }

    @Override
    protected String mutableObjectExpression(EditorData data,
            String sourceObjectExpression) {
        return sourceObjectExpression;
    }

    /**
     * Implement additional methods defined in {@link AbstractUiCommonModelEditorDriver}
     */
    @Override
    protected void writeAdditionalContent(TreeLogger logger,
            GeneratorContext context,
            EditorModel model,
            SourceWriter sw)
            throws UnableToCompleteException {
        TypeOracle typeOracle = context.getTypeOracle();
        entityModelType = typeOracle.findType("org.ovirt.engine.ui.uicommonweb.models.EntityModel");
        listModelType = typeOracle.findType("org.ovirt.engine.ui.uicommonweb.models.ListModel");
        this.logger = logger;
        this.model = model;
        this.sw = sw;

        logger.log(Type.DEBUG, "Strating to write additional Driver code");
        writeListenerMap();
        writeEventMap();
        writeOwnerModels();
    }

    /**
     * Writes the UiCommonListenerMap for the edited model
     */
    private void writeListenerMap() {
        logger.log(Type.DEBUG, "Strating to write ListenerMap");
        sw.println();
        sw.println("@Override");
        sw.println("protected " + UiCommonListenerMap.class.getName() + " getListenerMap() {");
        sw.indent();

        sw.println(UiCommonListenerMap.class.getName() + " listenerMap = new " + UiCommonListenerMap.class.getName()
                + "();");
        sw.println();

        logger.log(Type.DEBUG, "Looking for top-level Editor Fields");

        for (EditorData editorData : model.getEditorData()) {
            logger.log(Type.DEBUG, "Going over Field: " + editorData);
            String path = editorData.getPath();
            // Change first letter to Upper to comply with UiCommon Property Names
            path = Character.toUpperCase(path.charAt(0)) + path.substring(1, path.length());

            if (path.length() == 0) {
                continue;
            }

            // only relevant for top-level properties
            if (!editorData.isDeclaredPathNested()) {
                logger.log(Type.DEBUG, "Found top-level Field: " + editorData);

                sw.println("listenerMap.addListener(\"%s\", \"PropertyChanged\", new org.ovirt.engine.core.compat.IEventListener() {",
                        path);
                sw.indent();
                sw.println("@Override");
                sw.println("public void eventRaised(org.ovirt.engine.core.compat.Event ev, Object sender, org.ovirt.engine.core.compat.EventArgs args) {");
                sw.indent();
                sw.println("getEditor().%s.setValue(getObject()%s);",
                        editorData.getExpression(),
                        editorData.getGetterExpression());
                sw.outdent();
                sw.println("}");
                sw.outdent();
                sw.println("});");
                sw.println();
            }
        }

        sw.println("return listenerMap;");
        sw.outdent();
        sw.println("}");
    }

    /**
     * Writes the UiCommonEventMap for the edited model
     */
    private void writeEventMap() {
        logger.log(Type.DEBUG, "Strating to write EventMap");

        sw.println();
        sw.println("@Override");
        sw.println("protected " + UiCommonEventMap.class.getName() + " getEventMap() {");
        sw.indent();

        sw.println(UiCommonEventMap.class.getName() + " eventMap = new " + UiCommonEventMap.class.getName() + "();");

        logger.log(Type.DEBUG, "Looking for Model Fields");

        for (EditorData editorData : model.getEditorData()) {
            logger.log(Type.DEBUG, "Going over Field: " + editorData);

            String path = editorData.getPath();
            if (path.length() == 0) {
                continue;
            }

            JClassType propertyOwnerType = editorData.getPropertyOwnerType();

            if (propertyOwnerType == entityModelType) {
                logger.log(Type.DEBUG, "Found EntityModel Field: " + editorData);

                sw.println("eventMap.addEvent(\"%s\", \"EntityChanged\", getObject()%s.getEntityChangedEvent());",
                        path, editorData.getBeanOwnerExpression());

            } else if (propertyOwnerType == listModelType) {
                logger.log(Type.DEBUG, "Found ListModel Field: " + editorData);

                sw.println("eventMap.addEvent(\"%s\", \"ItemsChanged\", getObject()%s.getItemsChangedEvent());",
                        path, editorData.getBeanOwnerExpression());
                sw.println("eventMap.addEvent(\"%s\", \"SelectedItemsChanged\", getObject()%s.getSelectedItemsChangedEvent());",
                        path,
                        editorData.getBeanOwnerExpression());
                sw.println("eventMap.addEvent(\"%s\", \"SelectedItemChanged\", getObject()%s.getSelectedItemChangedEvent());",
                        path,
                        editorData.getBeanOwnerExpression());
            }
        }

        sw.println("return eventMap;");
        sw.outdent();
        sw.println("}");
    }

    /**
     * Writes the map of the owner Models
     */
    private void writeOwnerModels() {
        logger.log(Type.DEBUG, "Strating to write OwnerModels");
        sw.println();
        sw.println("@Override");
        sw.println("protected java.util.Map<String, org.ovirt.engine.ui.uicommonweb.models.EntityModel> getOwnerModels() {");
        sw.indent();

        sw.println("java.util.Map<String, org.ovirt.engine.ui.uicommonweb.models.EntityModel> regs = new java.util.HashMap<String, org.ovirt.engine.ui.uicommonweb.models.EntityModel>();");

        logger.log(Type.DEBUG, "Going over Editor Fields");
        for (EditorData editorData : model.getEditorData()) {
            logger.log(Type.DEBUG, "Going over Field: " + editorData);
            String path = editorData.getPath();
            if (path.length() == 0) {
                continue;
            }

            JClassType propertyOwnerType = editorData.getPropertyOwnerType();

            if (propertyOwnerType == listModelType || propertyOwnerType == entityModelType) {
                logger.log(Type.DEBUG, "Found owner Model Field: " + editorData);
                sw.println("regs.put(\"%s\", getObject()%s);", path, editorData.getBeanOwnerExpression());
            }
        }
        sw.println("return regs;");
        sw.outdent();
        sw.println("}");

    }
}
