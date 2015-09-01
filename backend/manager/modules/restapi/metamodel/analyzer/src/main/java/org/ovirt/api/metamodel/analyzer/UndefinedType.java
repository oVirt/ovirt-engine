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

import org.ovirt.api.metamodel.concepts.Type;

/**
 * This class represents a type whose definition is not known yet, only its name is known. It is intended to be used
 * while parsing a model, as some types may be referenced before they are used. Once the model is completely parsed
 * these types should be replaced with the real ones.
 */
public class UndefinedType extends Type {
}

