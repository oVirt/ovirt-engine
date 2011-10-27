package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.compat.Guid;

public interface DiskImageTemplateDAO extends GenericDao<DiskImageTemplate, Guid> {

    /**
     * Retrieves the template with the specified VM and IT id.
     *
     * @param vm
     *            the VM id
     * @param template
     *            the template id
     */
    DiskImageTemplate getByVmTemplateAndId(Guid vm, Guid template);

    /**
     * Retrieves all templates related to the given VM template id.
     *
     * @param vmTemplate
     *            the VM template id
     * @return the list of templates
     */
    List<DiskImageTemplate> getAllByVmTemplate(Guid vmTemplate);

}
