package org.ovirt.engine.core.common.businessentities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

public class BusinessEntityMapTest {

    @Test
    public void testDontFailWithNullValueEntitiesList() throws Exception {
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(null);
        assertThat(map.get("anyString"), nullValue());
        assertThat(map.get(Guid.newGuid()), nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatingBusinessEntityMapWithDuplicatesAmongIds() throws Exception {
        Guid itemId = Guid.newGuid();
        TestItem first = new TestItem(itemId, "name");
        TestItem second = new TestItem(itemId, "different name");
        new BusinessEntityMap<>(Arrays.asList(first, second));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatingBusinessEntityMapWithDuplicatesAmongName() throws Exception {
        TestItem first = new TestItem(Guid.newGuid(), "name");
        TestItem second = new TestItem(Guid.newGuid(), "name");
        new BusinessEntityMap<>(Arrays.asList(first, second));
    }

    @Test
    public void testCreatingBusinessEntityMapWithNullDuplicatesOnly() throws Exception {
        TestItem first = new TestItem(null, null);
        TestItem second = new TestItem(null, null);
        new BusinessEntityMap<>(Arrays.asList(first, second));
    }

    @Test
    public void testGetByNameReturnsItemOfThatName() throws Exception {
        String itemName = "name";
        TestItem item = new TestItem(Guid.newGuid(), itemName);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Arrays.asList(item));

        assertThat(map.get(itemName), is(item));
    }

    @Test
    public void testGetByIdReturnsItemOfThatId() throws Exception {
        Guid itemId = Guid.newGuid();
        TestItem testItem = new TestItem(itemId, null);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Arrays.asList(testItem));

        assertThat(map.get(itemId), is(testItem));
    }

    @Test
    public void testGetByNameReturnsNullIfNotExist() throws Exception {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Arrays.asList(testItem));

        assertThat(map.get("different name"), nullValue());
    }

    @Test
    public void testGetByIdReturnsNullIfNotExist() throws Exception {
        TestItem testItem = new TestItem(Guid.newGuid(), null);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Arrays.asList(testItem));

        assertThat(map.get(Guid.newGuid()), nullValue());
    }

    @Test
    public void testContainsKeyReturnsTrueForExistingId() throws Exception {
        Guid itemId = Guid.newGuid();
        List<TestItem> testItems = Collections.singletonList(new TestItem(itemId, null));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey(itemId), is(true));
    }

    @Test
    public void testContainsKeyReturnsFalseForNotExistingId() throws Exception {
        List<TestItem> testItems = Collections.singletonList(new TestItem(Guid.newGuid(), null));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey(Guid.newGuid()), is(false));
    }

    @Test
    public void testContainsKeyReturnsTrueForExistingName() throws Exception {
        String name = "name";
        List<TestItem> testItems = Collections.singletonList(new TestItem(null, name));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey(name), is(true));
    }

    @Test
    public void testContainsKeyReturnsFalseForNotExistingName() throws Exception {
        List<TestItem> testItems = Collections.singletonList(new TestItem(null, "name"));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey("different name"), is(false));
    }

    @Test
    public void testGetByIdOrNameWhenIdIsSpecified() throws Exception {
        Guid itemId = Guid.newGuid();
        TestItem testItem = new TestItem(itemId, "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(itemId, "different name"), is(testItem));
    }

    @Test
    public void testGetByIdOrNameWhenIdIsNotSpecified() throws Exception {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(null, "name"), is(testItem));
    }

    @Test
    public void testGetByIdOrNameWhenItemIsNotIdentified() throws Exception {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(null, "different name"), nullValue());
    }

    @Test
    public void testGetByIdOrNameWhenItemIsNotIdentified2() throws Exception {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(null, null), nullValue());
    }

    private static class TestItem implements BusinessEntity<Guid>, Nameable {

        private Guid id;
        private String name;

        private TestItem(Guid id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public Guid getId() {
            return id;
        }

        @Override
        public void setId(Guid id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


    }
}
