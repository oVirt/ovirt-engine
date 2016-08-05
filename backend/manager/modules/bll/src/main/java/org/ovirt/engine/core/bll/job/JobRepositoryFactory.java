package org.ovirt.engine.core.bll.job;

import org.ovirt.engine.core.di.Injector;

/**
 * Factory responsible for instantiating the {@link JobRepository}
 * @deprecated Please inject {@link JobRepository} directly.
 */
@Deprecated
public class JobRepositoryFactory {

    public static JobRepository getJobRepository() {
        return Injector.get(JobRepository.class);
    }

}
