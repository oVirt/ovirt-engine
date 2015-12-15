package org.ovirt.api.metamodel.tool;

import java.io.IOException;
import java.util.Iterator;
import javax.inject.Inject;

import org.ovirt.api.metamodel.concepts.EnumType;
import org.ovirt.api.metamodel.concepts.EnumValue;
import org.ovirt.api.metamodel.concepts.Model;
import org.ovirt.api.metamodel.concepts.Type;

public class EnumGenerator extends JavaGenerator {

    @Inject
    private JavaPackages javaPackages;
    @Inject
    private JavaNames javaNames;
    @Inject
    private SchemaNames schemaNames;

    // The buffer used to generate the source code:
    private JavaClassBuffer javaBuffer;

    public void generate(Model model) {
        for (Type type : model.getTypes()) {
            //only generate enums which are not generated through the schema.
            //(enums which are generated through the schema are an exception
            //to the rule and exist for historic reasons. They are: StatisticKind,
            //StatisticUnit, ValueType.
            if (type instanceof EnumType && !schemaNames.isSchemaEnum(type)) {
                EnumType enumType = (EnumType) type;
                generateEnum(enumType);
            }
        }
    }

    private void generateEnum(EnumType type) {
        javaBuffer = new JavaClassBuffer();
        JavaClassName enumName = getEnumName(type);
        javaBuffer.setClassName(enumName);
        generateEnumSource(type);
        try {
            javaBuffer.write(outDir);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    private JavaClassName getEnumName(EnumType type) {
        JavaClassName enumName = new JavaClassName();
        enumName.setPackageName(javaPackages.getXjcPackageName());
        enumName.setSimpleName(javaNames.getJavaClassStyleName(type.getName()));
        return enumName;
    }

    private void generateEnumSource(EnumType enumType) {
        String enumName = javaBuffer.getClassName().getSimpleName();
        writeImports();
        writeClassHeader(enumName);
        writeEnumValues(enumType);
        writeLogger(enumName);
        writeValueMethod();
        writeFromValueMethod(enumName, enumType);
        writeClassClose();
    }

    private void writeImports() {
        javaBuffer.addLine("import org.slf4j.Logger;");
        javaBuffer.addLine("import org.slf4j.LoggerFactory;");
        javaBuffer.addLine();
    }

    private void writeClassHeader(String enumName) {
        javaBuffer.addLine("public enum %s {", enumName);
        javaBuffer.addLine();
    }

    private void writeLogger(String enumName) {
        javaBuffer.addLine("private static final Logger LOG = LoggerFactory.getLogger(" + enumName + ".class);");
        javaBuffer.addLine();
    }

    private void writeEnumValues(EnumType type) {
        Iterator<EnumValue> iterator = type.getValues().iterator();
        while (iterator.hasNext()) {
            EnumValue value = iterator.next();
            javaBuffer.addLine(getEnumValueLine(value, !iterator.hasNext()));
        }
        javaBuffer.addLine();
    }

    private String getEnumValueLine(EnumValue enumValue, boolean lastValue) {
        String value = enumValue.getName().toString().toUpperCase();
        return value + (lastValue ? ";" : ",");
    }

    private void writeValueMethod() {
        javaBuffer.addLine("public String value() {");
        javaBuffer.addLine("return name().toLowerCase();");
        javaBuffer.addLine("}");
        javaBuffer.addLine();
    }

    private void writeFromValueMethod(String enumName, EnumType enumType) {
        javaBuffer.addLine("public static " + enumName + " fromValue(String value) {");
        javaBuffer.addLine("try {");
        javaBuffer.addLine("return valueOf(value.toUpperCase());");
        javaBuffer.addLine("}");
        javaBuffer.addLine("catch (IllegalArgumentException e) {");
        javaBuffer.addLine("LOG.error(\"" + nonExistingValueMessage(enumName, enumType)  + "\");");
        javaBuffer.addLine("return null;");
        javaBuffer.addLine("}");
        javaBuffer.addLine("}");
    }

    private String nonExistingValueMessage(String enumName, EnumType enumType) {
        StringBuilder builder = new StringBuilder();
        builder.append("The string '\" + value + \"' isn't a valid value for the '")
                .append(enumName)
                .append("' enumerated type. ")
                .append("Valid values are: ")
                .append(getValueValues(enumType))
                .append(".");
        return builder.toString();
    }

    private String getValueValues(EnumType enumType) {
        StringBuilder builder = new StringBuilder();
        for (EnumValue enumValue : enumType.getValues()) {
            builder.append("'")
            .append(enumValue.getName().toString().toLowerCase())
            .append("', ");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    private void writeClassClose() {
        javaBuffer.addLine();
        javaBuffer.addLine("}");
    }
}
