package org.ovirt.engine.core.common.businessentities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Guid;

public class BusinessEntityMapTest {

    @Test
    public void testDontFailWithNullValueEntitiesList() {
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(null);
        assertThat(map.get("anyString"), nullValue());
        assertThat(map.get(Guid.newGuid()), nullValue());
    }

    @Test
    public void testCreatingBusinessEntityMapWithDuplicatesAmongIds() {
        Guid itemId = Guid.newGuid();
        TestItem first = new TestItem(itemId, "name");
        TestItem second = new TestItem(itemId, "different name");
        assertThrows(IllegalArgumentException.class, () -> new BusinessEntityMap<>(Arrays.asList(first, second)));
    }

    @Test
    public void testCreatingBusinessEntityMapWithDuplicatesAmongName() {
        TestItem first = new TestItem(Guid.newGuid(), "name");
        TestItem second = new TestItem(Guid.newGuid(), "name");
        assertThrows(IllegalArgumentException.class, () -> new BusinessEntityMap<>(Arrays.asList(first, second)));
    }

    @Test
    public void testCreatingBusinessEntityMapWithNullDuplicatesOnly() {
        TestItem first = new TestItem(null, null);
        TestItem second = new TestItem(null, null);
        new BusinessEntityMap<>(Arrays.asList(first, second));
    }

    @Test
    public void testGetByNameReturnsItemOfThatName() {
        String itemName = "name";
        TestItem item = new TestItem(Guid.newGuid(), itemName);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Collections.singletonList(item));

        assertThat(map.get(itemName), is(item));
    }

    @Test
    public void testGetByIdReturnsItemOfThatId() {
        Guid itemId = Guid.newGuid();
        TestItem testItem = new TestItem(itemId, null);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Collections.singletonList(testItem));

        assertThat(map.get(itemId), is(testItem));
    }

    @Test
    public void testGetByNameReturnsNullIfNotExist() {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Collections.singletonList(testItem));

        assertThat(map.get("different name"), nullValue());
    }

    @Test
    public void testGetByIdReturnsNullIfNotExist() {
        TestItem testItem = new TestItem(Guid.newGuid(), null);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(Collections.singletonList(testItem));

        assertThat(map.get(Guid.newGuid()), nullValue());
    }

    @Test
    public void testContainsKeyReturnsTrueForExistingId() {
        Guid itemId = Guid.newGuid();
        List<TestItem> testItems = Collections.singletonList(new TestItem(itemId, null));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey(itemId), is(true));
    }

    @Test
    public void testContainsKeyReturnsFalseForNotExistingId() {
        List<TestItem> testItems = Collections.singletonList(new TestItem(Guid.newGuid(), null));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey(Guid.newGuid()), is(false));
    }

    @Test
    public void testContainsKeyReturnsTrueForExistingName() {
        String name = "name";
        List<TestItem> testItems = Collections.singletonList(new TestItem(null, name));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey(name), is(true));
    }

    @Test
    public void testContainsKeyReturnsFalseForNotExistingName() {
        List<TestItem> testItems = Collections.singletonList(new TestItem(null, "name"));
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.containsKey("different name"), is(false));
    }

    @Test
    public void testGetByIdOrNameWhenIdIsSpecified() {
        Guid itemId = Guid.newGuid();
        TestItem testItem = new TestItem(itemId, "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(itemId, "different name"), is(testItem));
    }

    @Test
    public void testGetByIdOrNameWhenIdIsNotSpecified() {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(null, "name"), is(testItem));
    }

    @Test
    public void testGetByIdOrNameWhenItemIsNotIdentified() {
        TestItem testItem = new TestItem(Guid.newGuid(), "name");

        List<TestItem> testItems = Collections.singletonList(testItem);
        BusinessEntityMap<TestItem> map = new BusinessEntityMap<>(testItems);
        assertThat(map.get(null, "different name"), nullValue());
    }

    @Test
    public void testGetByIdOrNameWhenItemIsNotIdentified2() {
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
