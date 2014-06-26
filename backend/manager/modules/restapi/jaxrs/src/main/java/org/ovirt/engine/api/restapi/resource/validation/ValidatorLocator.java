/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.common.util.PackageExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorLocator {
    /**
     * The logger used by this class.
     */
    protected static final Logger log = LoggerFactory.getLogger(ValidatorLocator.class);

    /**
     * The cache of loaded validators.
     */
    private Map<Class<?>, Validator<?>> validators = new HashMap<>();

    public void populate() {
        populate(this.getClass().getPackage().getName());
    }

    public void populate(String discoverPackageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> classNames = PackageExplorer.discoverClasses(discoverPackageName);
        for (String className : classNames) {
            try {
                @SuppressWarnings("unchecked")
                Class<Validator<?>> validatorClass = (Class<Validator<?>>) classLoader.loadClass(className);
                ValidatedClass validatedClass = validatorClass.getAnnotation(ValidatedClass.class);
                if (validatedClass != null) {
                    try {
                        Validator<?> validatorInstance = validatorClass.newInstance();
                        validators.put(validatedClass.clazz(), validatorInstance);
                    }
                    catch (InstantiationException|IllegalAccessException exception) {
                        log.error(
                            "Error while trying to create instance of validator class \"{}\".",
                            className,
                            exception
                        );
                    }
                }
            }
            catch (ClassNotFoundException exception) {
                log.error(
                    "Error while trying to load validator class \"{}\".",
                    className,
                    exception
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E> Validator<E> getValidator(Class<E> entityType) {
        return (Validator<E>) validators.get(entityType);
    }
}
