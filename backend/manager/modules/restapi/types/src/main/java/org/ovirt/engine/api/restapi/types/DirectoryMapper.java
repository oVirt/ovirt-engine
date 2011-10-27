package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.restapi.model.Directory;

public class DirectoryMapper {

    @Mapping(from = Domain.class, to = Directory.class)
    public static Directory map(Domain dir, Directory template) {
        Directory directoriesService = template != null ? template : new Directory();
        directoriesService.setDomain(dir.getName());
        directoriesService.setId(dir.getId());
        return directoriesService;
    }

    @Mapping(from = Directory.class, to = Domain.class)
    public static Domain map(Directory dir, Domain template) {
        Domain directory = template != null ? template : new Domain();
        directory.setName(dir.getDomain());
        directory.setId(dir.getId());
        return directory;
    }
}
