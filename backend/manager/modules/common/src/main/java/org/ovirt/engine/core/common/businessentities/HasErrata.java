package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

/**
 * A business entity that can have errata. Presently, these are machine types only
 * (VM, Host, and the engine itself [although there is no entity for that]).
 */
public interface HasErrata extends Nameable, BusinessEntity<Guid> {

}
