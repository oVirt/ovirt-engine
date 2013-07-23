// This class has been copied from the GWT 2.5.1 source code because it
// is used by the GWT RPC but it isn't included in the gwt-servlet.jar.
// The alternative is to include the gwt-dev.jar file in the runtime
// class path of the application and that introduces many class loading
// problems caused by the fact that gwt-dev.jar bundles many third party
// dependencies, including its own version of the Xerces XML parser.

/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.core.client;

/**
 * When running in Development Mode, acts as a bridge from {@link GWT} into the
 * Development Mode environment.
 *
 * For code that may run anywhere besides the client, use
 * {@link com.google.gwt.core.shared.GWTBridge} instead.
 */
public abstract class GWTBridge extends com.google.gwt.core.shared.GWTBridge {
}
