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

package services;

import org.ovirt.api.metamodel.annotations.In;
import org.ovirt.api.metamodel.annotations.Out;
import org.ovirt.api.metamodel.annotations.Service;
import types.StorageDomain;
import types.Template;

@Service
public interface TemplateService {
    interface Export {
        @In Boolean exclussive();
        @In StorageDomain storageDomain();
    }

    interface Get {
        @Out Template template();
    }

    interface Update {
        @In @Out Template template();
    }

    interface Remove {
    }

    @Service AssignedPermissionsService permissions();
    @Service AssignedTagsService tags();
    @Service GraphicsConsolesService graphicsConsoles();
    @Service TemplateCdromsService cdroms();
    @Service TemplateDisksService disks();
    @Service TemplateNicsService nics();
    @Service TemplateWatchdogsService watchdogs();
}
