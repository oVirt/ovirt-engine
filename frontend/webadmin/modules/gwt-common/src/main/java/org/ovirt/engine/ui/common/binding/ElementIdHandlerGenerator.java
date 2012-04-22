package org.ovirt.engine.ui.common.binding;

import java.io.PrintWriter;

import org.ovirt.engine.ui.common.idhandler.BaseElementIdHandler;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * GWT deferred binding generator that provides {@link org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler} implementations.
 *
 * @see org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler
 */
public class ElementIdHandlerGenerator extends Generator {

    static final String ElementIdHandler_generateAndSetIds_owner = "owner"; //$NON-NLS-1$

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
            throws UnableToCompleteException {
        TypeOracle oracle = context.getTypeOracle();
        JClassType toGenerate = oracle.findType(typeName).isInterface();
        if (toGenerate == null) {
            logger.log(TreeLogger.ERROR, typeName + " is not an interface type"); //$NON-NLS-1$
            throw new UnableToCompleteException();
        }

        ElementIdTypeParser parser = new ElementIdTypeParser(logger, toGenerate);
        ElementIdStatement[] statements = parser.parseStatements();

        String packageName = toGenerate.getPackage().getName();
        String simpleSourceName = toGenerate.getName().replace('.', '_') + "Impl"; //$NON-NLS-1$
        PrintWriter pw = context.tryCreate(logger, packageName, simpleSourceName);
        if (pw == null) {
            return packageName + "." + simpleSourceName; //$NON-NLS-1$
        }

        JClassType superclass = oracle.findType(BaseElementIdHandler.class.getName()).isClass();
        assert superclass != null : "No BaseElementIdHandler type"; //$NON-NLS-1$

        ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(
                packageName, simpleSourceName);
        factory.setSuperclass(superclass.getQualifiedSourceName()
                + "<" + parser.getOwnerType().getParameterizedQualifiedSourceName() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        factory.addImplementedInterface(typeName);

        SourceWriter sw = factory.createSourceWriter(context, pw);
        writeGenerateAndSetIds(sw, parser.getOwnerType(), statements);
        sw.commit(logger);

        return factory.getCreatedClassName();
    }

    void writeGenerateAndSetIds(SourceWriter sw, JClassType ownerType, ElementIdStatement[] statements) {
        sw.println("@Override public void generateAndSetIds(%s %s) {", //$NON-NLS-1$
                ownerType.getQualifiedSourceName(), ElementIdHandler_generateAndSetIds_owner);
        sw.indent();

        for (ElementIdStatement st : statements) {
            sw.println(String.format("if (%s) %s;", //$NON-NLS-1$
                    st.buildGuardCondition(), st.buildIdSetterStatement()));
        }

        sw.outdent();
        sw.println("}"); //$NON-NLS-1$
    }

}
