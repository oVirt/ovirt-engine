package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.Bookmark;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.utils.ReflectionUtils;

public class GeneralDbDAOTest extends BaseDAOTestCase {

    private static Log logger = LogFactory.getLog(GeneralDbDAOTest.class);
    private static List<Class> businessEntities = new ArrayList<Class>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Only classes that allow user input are checked.
        businessEntities.add(Bookmark.class);
        businessEntities.add(DbUser.class);
        businessEntities.add(LUNs.class);
        businessEntities.add(NetworkInterface.class);
        businessEntities.add(Network.class);
        businessEntities.add(Role.class);
        businessEntities.add(StorageDomainStatic.class);
        businessEntities.add(storage_pool.class);
        businessEntities.add(tags.class);
        businessEntities.add(VDSGroup.class);
        businessEntities.add(VdsStatic.class);
        businessEntities.add(vm_pools.class);
        businessEntities.add(VmStatic.class);
        businessEntities.add(VmTemplate.class);
    }

    /**
     * Tests that Size annotation on String BE fields match size in the database.
     */
    @Test
    public void testBusinessEntitiesColumnSize() {
        String table;
        String column;
        List<String> errors = new ArrayList<String>();
        // Get all business entities
        for (Class businessEntity : businessEntities) {
            // get BE table from class annotation
            Table tableAnnotation = (Table) ReflectionUtils.getTableAnnotation(businessEntity);
            if (tableAnnotation != null) {
                table = tableAnnotation.name();
                // get all BE fields
                Field[] fields = businessEntity.getDeclaredFields();
                for (Field field : fields) {
                    // get Column annotation
                    Column columnAnnotation = (Column) ReflectionUtils.getColumnAnnotation(field);
                    if (columnAnnotation != null) {
                        // get column name
                        column = columnAnnotation.name();
                        int sizeInDb = dbFacade.getColumnSize(table, column);
                        // get Size annotation
                        Size sizeAnnotation = (Size) ReflectionUtils.getSizeAnnotation(field);
                        if (sizeAnnotation != null) {
                            // compare size in DB with size in BE
                            int sizeInBusinessEntity = sizeAnnotation.max();
                            if (sizeInDb != -1 && (sizeInDb != sizeInBusinessEntity)) {
                                errors.add(String.format("Class [%s] Field [%s]: Table [%s] Column [%s] size in DB: [%d] does not match size in Business Entity: [%d]",
                                        businessEntity.getName(),
                                        businessEntity.getName(),
                                        table,
                                        column,
                                        sizeInDb,
                                        sizeInBusinessEntity));
                            }
                        } else { // report on String fields that have no Size annotation.
                            if (field.getType().equals(String.class)) {
                                errors.add(String.format("Class [%s] Field [%s] should have @Size(max = %d) annotation",
                                        businessEntity.getName(),
                                        field.getName(),
                                        sizeInDb));
                            }
                        }
                    }
                }
            }
        }
        for (String err : errors) {
            logger.error(err);
        }
        assertTrue(errors.size() == 0);
    }
}
