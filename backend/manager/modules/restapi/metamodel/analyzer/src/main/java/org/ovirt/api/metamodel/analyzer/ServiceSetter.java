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

package org.ovirt.api.metamodel.analyzer;

import org.ovirt.api.metamodel.concepts.Service;

import java.util.function.Consumer;

/**
 * This interface represents a function that can be used to change the reference to a service. For example, lets assume
 * that the service {@code MyService} has been referenced from another service, but not yet defined. The code tha
 * analyzes the service will probably create a dummy service and it will need to remember to replace that dummy service
 * with the real service once it is known. To do so it can do the following:
 *
 * <pre>
 * // Create the locator model using a dummy service:
 * Locator locator = ...;
 * Service dummy = ...;
 * attribute.setType(dummy);
 *
 * // Remember to replace that type later:
 * LocatorSetter setter = attribute::setType;
 *
 * ...
 *
 * // Once we know what is the real service we can replace it:
 * Service real = ...;
 * setter.accept(real);
 * </pre>
 *
 * These service setters can be easily remembered, for example using a list or a map.
 */
public interface ServiceSetter extends Consumer<Service> {
}
