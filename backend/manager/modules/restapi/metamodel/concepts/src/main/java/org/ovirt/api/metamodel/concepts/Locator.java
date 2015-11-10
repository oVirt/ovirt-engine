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

package org.ovirt.api.metamodel.concepts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * A locator is reference from one service to another service.
 */
public class Locator extends Concept {
    // The service that is resolved by this locator:
    private Service service;

    // The list of parameters:
    private List<Parameter> parameters = new ArrayList<>();

    /**
     * Returns the list of parameters of this locator. The returned list is a copy of the one used internally, so it is
     * safe * to modify it in any way. If you aren't going to modify the list consider using the {@link #parameters()}
     * method instead.
     */
    public List<Parameter> getParameters() {
        return new CopyOnWriteArrayList<>(parameters);
    }

    /**
     * Returns a stream that delivers the parameters of this locator.
     */
    public Stream<Parameter> parameters() {
        return parameters.stream();
    }

    /**
     * Adds a new parameter to this locator.
     */
    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Adds a list of parameters to this locator.
     */
    public void addParameters(Parameter parameter) {
        parameters.add(parameter);
    }

    /**
     * Returns the service that is resolved by this locator.
     */
    public Service getService() {
        return service;
    }

    /**
     * Sets the service that is resolved by this locator.
     */
    public void setService(Service newService) {
        service = newService;
    }
}

