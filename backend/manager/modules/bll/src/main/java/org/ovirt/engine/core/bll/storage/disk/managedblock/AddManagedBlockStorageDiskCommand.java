package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@InternalCommandAttribute
public class AddManagedBlockStorageDiskCommand<T extends AddDiskParameters> extends CommandBase<T> {

    @Inject
    private ResourceManager resourceManager;

    public AddManagedBlockStorageDiskCommand(Guid commandId) {
        super(commandId);
    }

    public AddManagedBlockStorageDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        setSucceeded(true);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getParameters().getStorageDomainId()));
    }
}
