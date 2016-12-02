/*
Copyright (c) 2017 Red Hat, Inc.

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

package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddDeprecatedApiEventParameters;

@SuppressWarnings("unused")
public class AddDeprecatedApiEventCommand<T extends AddDeprecatedApiEventParameters> extends CommandBase<T> {
    public AddDeprecatedApiEventCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.DEPRECATED_API;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    @Override
    protected void executeCommand() {
        T parameters = getParameters();

        // Use the client address as the custom id of the audit log message. That way the flood prevention mechanism
        // will automatically discard duplicated messages.
        setCustomId(parameters.getClientAddress());

        // Populate the audit log message with the details:
        addCustomValue("ApiVersion", parameters.getApiVersion());
        addCustomValue("ClientAddress", parameters.getClientAddress());
        addCustomValue("DeprecatingVersion", parameters.getDeprecatingVersion());
        addCustomValue("RemovingVersion", parameters.getRemovingVersion());

        setSucceeded(true);
    }

    @Override
    protected boolean isUserAuthorizedToRunAction() {
        return true;
    }
}
