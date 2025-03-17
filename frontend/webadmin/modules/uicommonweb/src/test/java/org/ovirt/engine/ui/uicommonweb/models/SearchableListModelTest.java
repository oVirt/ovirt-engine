package org.ovirt.engine.ui.uicommonweb.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetupExtension;

@ExtendWith(UiCommonSetupExtension.class)
public class SearchableListModelTest {
    @Test
    public void testSelectionRestoredOnNewSetItems() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setItems(Arrays.asList(1, 2, 3));
        listModel.setSelectedItem(2);

        listModel.setItems(Arrays.asList(1, 2));

        assertEquals((Integer) 2, listModel.getSelectedItem());
    }

    @Test
    public void selectionCleared() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setItems(Arrays.asList(1, 2, 3));
        listModel.setSelectedItem(2);
        listModel.setSelectedItems(new ArrayList<Integer>(Arrays.asList(2)));

        listModel.setItems(null);

        listModel.setItems(Arrays.asList(1, 2, 3));

        assertEquals(null, listModel.getSelectedItem());
        assertThat(listModel.getSelectedItems()).isEmpty();
    }

    @Test
    void whenNullHiddenSearchStringThenCorrectModifiedSearchString() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setDefaultSearchString("VM:"); //$NON-NLS-1$
        listModel.setHiddenSearchString(null);
        listModel.setSearchString(listModel.getDefaultSearchString());

        assertEquals(listModel.getSearchString(), listModel.getModifiedSearchString());
    }

    @Test
    void whenEmptyHiddenSearchStringThenCorrectModifiedSearchString() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setDefaultSearchString("VM:"); //$NON-NLS-1$
        listModel.setHiddenSearchString(""); //$NON-NLS-1$
        listModel.setSearchString(listModel.getDefaultSearchString());

        assertEquals(listModel.getSearchString(), listModel.getModifiedSearchString());
    }

    @Test
    void whenNotEmptyHiddenSearchStringThenCorrectModifiedSearchString() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setDefaultSearchString("VM:"); //$NON-NLS-1$
        listModel.setHiddenSearchString("name=vm-1"); //$NON-NLS-1$
        listModel.setSearchString(listModel.getDefaultSearchString());

        assertEquals("VM:name=vm-1", listModel.getModifiedSearchString()); //$NON-NLS-1$
    }

    @Test
    void whenNotEmptyHiddenSearchStringAndUserSearchStringThenCorrectModifiedSearchString() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setDefaultSearchString("VM:"); //$NON-NLS-1$
        listModel.setHiddenSearchString("name=vm-1"); //$NON-NLS-1$
        listModel.setSearchString("VM:name=vm-2"); //$NON-NLS-1$

        assertEquals("VM:name=vm-2 AND name=vm-1", listModel.getModifiedSearchString()); //$NON-NLS-1$
    }

    @Test
    void whenNotEmptyHiddenSearchStringAndUserSearchStringWithTagsThenCorrectModifiedSearchString() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setDefaultSearchString("VM:"); //$NON-NLS-1$
        listModel.setHiddenSearchString("name=vm-1"); //$NON-NLS-1$
        listModel.setSearchString("VM:name=vm-2 or cluster=Default"); //$NON-NLS-1$
        listModel.setTagStrings(Arrays.asList("tag-1", "tag-2")); //$NON-NLS-1$ //$NON-NLS-2$

        assertEquals("VM:name=vm-2 AND name=vm-1 OR cluster=Default AND name=vm-1 OR tag=tag-1 AND name=vm-1 OR tag=tag-2 AND name=vm-1", listModel.getModifiedSearchString()); //$NON-NLS-1$
    }

    @Test
    void whenNotEmptyHiddenSearchStringAndUserSearchStringWithDifferentOrExpressionsThenCorrectModifiedSearchString() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class,
                        withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setDefaultSearchString("VM:"); //$NON-NLS-1$
        listModel.setHiddenSearchString("name=vm-1"); //$NON-NLS-1$
        listModel.setSearchString("VM:name=vm-2 OR cluster=Default oR name=vm-3 Or name=vm-4"); //$NON-NLS-1$

        assertEquals("VM:name=vm-2 AND name=vm-1 OR cluster=Default AND name=vm-1 OR name=vm-3 AND name=vm-1 OR name=vm-4 AND name=vm-1", listModel.getModifiedSearchString()); //$NON-NLS-1$
    }
}
