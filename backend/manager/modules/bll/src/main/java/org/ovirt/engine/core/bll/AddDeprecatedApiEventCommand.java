/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
