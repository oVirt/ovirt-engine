package org.ovirt.engine.core.common.utils.ansible;

import java.nio.file.Path;
import java.util.List;

/**
 * Read-only ansible command inventory file configuration view
 */
public interface InventoryFileConfig {

    List<String> hostnames();

    Path inventoryFile();
}
