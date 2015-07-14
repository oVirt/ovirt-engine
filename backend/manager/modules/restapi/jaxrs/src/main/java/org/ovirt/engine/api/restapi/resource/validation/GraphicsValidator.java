package org.ovirt.engine.api.restapi.resource.validation;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.GraphicsType;

@ValidatedClass(clazz = GraphicsConsole.class)
public class GraphicsValidator implements Validator<GraphicsConsole> {

    @Override
    public void validateEnums(GraphicsConsole graphicsConsole) {
        if (graphicsConsole != null) {
            if (graphicsConsole.isSetProtocol()) {
                validateEnum(GraphicsType.class, graphicsConsole.getProtocol(), true);
            }
        }
    }
}
