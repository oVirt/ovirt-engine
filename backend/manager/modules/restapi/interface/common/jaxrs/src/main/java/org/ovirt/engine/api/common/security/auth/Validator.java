/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.security.auth;

public interface Validator {

    /**
     * By default principal validation is lazy, with the assumption
     * that this will be initiated by the resource later on the
     * dispatch path. This method allows subclasses to pursue an
     * alternate strategy based on eager validation.  The injected
     * validator, if present, will be called immediately after the
     * credentials have been decoded.
     *
     * @param principal  the decoded principal
     * @return           true iff dispatch should continue
     */
    boolean validate(Principal principal, String sessionId);

    Principal validate(String sessionID);

    void usePersistentSession(boolean persistentSession);

}
