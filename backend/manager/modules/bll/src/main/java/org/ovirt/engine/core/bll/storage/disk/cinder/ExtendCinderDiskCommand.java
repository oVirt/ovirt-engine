package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.UpdateDiskCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InternalCommandAttribute
public class ExtendCinderDiskCommand<T extends UpdateDiskParameters> extends UpdateDiskCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ExtendCinderDiskCommand.class);

    @Inject
    private ImageDao imageDao;

    public ExtendCinderDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void executeCommand() {
        CinderDisk cinderDisk = (CinderDisk) getNewDisk();
        getCinderBroker().extendDisk(cinderDisk, (int) getNewDiskSizeInGB());

        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(cinderDisk.getId());
        setSucceeded(true);
    }

    protected void performDiskUpdate() {
        CinderDisk cinderDisk = (CinderDisk) getNewDisk();
        imageDao.updateImageSize(cinderDisk.getImageId(), cinderDisk.getSize());
        super.performDiskUpdate();
    }

    @Override
    public CommandCallback getCallback() {
        return Injector.injectMembers(new ExtendCinderDiskCommandCallback());
    }

    @Override
    public boolean validate() {
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
    public Guid getStorageDomainId() {
        CinderDisk cinderDisk = (CinderDisk) getNewDisk();
        return cinderDisk.getStorageIds().get(0);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage,
                getStorageDomainId()));
    }
}
