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

import org.ovirt.api.metamodel.annotations.In;
import org.ovirt.api.metamodel.annotations.Link;
import org.ovirt.api.metamodel.annotations.Method;
import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Root;
import org.ovirt.api.metamodel.annotations.Service;
import org.ovirt.api.metamodel.annotations.Type;

/**
 * During the analysis of the model the names of the annotations will be used frequently. To avoid importing them in
 * many places we extract the names here.
 */
public class ModelAnnotations {
    public static final String IN = In.class.getName();
    public static final String LINK = Link.class.getName();
    public static final String METHOD = Method.class.getName();
    public static final String OUT = Out.class.getName();
    public static final String ROOT = Root.class.getName();
    public static final String SERVICE = Service.class.getName();
    public static final String TYPE = Type.class.getName();
}
