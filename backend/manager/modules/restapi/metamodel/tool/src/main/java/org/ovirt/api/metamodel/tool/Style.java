/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.api.metamodel.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * CDI qualifier used to select a specific naming style. It is initially intended to work together with the
 * {@link JavaNames} interface, in order to select the desired implementation. For example, the following will
 * select the implementation of {@link JavaNames} that is annotated with {@Style("versioned")} instead of the
 * default one:
 *
 * <pre>
 * @Inject
 * @Style("versioned")
 * private JavaNames javaNames;
 * </pre>
 *
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface Style {
    /**
     * The naming style, for example <i>versioned</i>.
     */
    String value();
}
