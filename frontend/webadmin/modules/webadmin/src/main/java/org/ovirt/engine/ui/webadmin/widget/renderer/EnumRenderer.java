package org.ovirt.engine.ui.webadmin.widget.renderer;

import java.util.MissingResourceException;
import java.util.logging.Logger;

import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Enum values.
 *
 * @param <E>
 *            Enum type.
 */
public class EnumRenderer<E extends Enum<E>> extends AbstractRenderer<E> {

    private static final Logger logger = Logger.getLogger(EnumRenderer.class.getName());

    @Override
    public String render(E object) {
        if (object == null) {
            return "null";
        }
        Translator translator = EnumTranslator.Create(object.getDeclaringClass());
        String translation = object.name();

        try {
            translation = translator.get(object);
        } catch (MissingResourceException e) {
            // Silently ignore missing resource
            logger.info("Missing Enum resource: " + e.getLocalizedMessage());
        }

        return translation;
    }

}
