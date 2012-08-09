package org.ovirt.engine.api.restapi.resource.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.common.util.PackageExplorer;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ValidatorLocator {

    protected static final Log LOG = LogFactory.getLog(ValidatorLocator.class);
    private Map<Class<?>, Validator<?>> validators = new HashMap<Class<?>, Validator<?>>();

    public void populate() {
        populate(this.getClass().getPackage().getName());
    }

    public void populate(String discoverPackageName) {
        List<Class<?>> classes = PackageExplorer.discoverClasses(discoverPackageName);
        for (Class<?> clz : classes) {
            ValidatedClass validatedClass = clz.getAnnotation(ValidatedClass.class);
            if (validatedClass != null) {
                try {
                    validators.put(validatedClass.clazz(),
                            (Validator<?>) clz.newInstance());
                } catch (Exception e) {
                    LOG.error("Problem initializing Enum Validators", e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E> Validator<E> getValidator(Class<E> entityType) {
        return (Validator<E>) validators.get(entityType);
    }
}
