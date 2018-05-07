package org.ovirt.engine.ui.uicommonweb.models;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Answers;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetup;

public class SearchableListModelTest {

    @ClassRule
    public static UiCommonSetup setup = new UiCommonSetup();

    @Test
    public void testSelectionRestoredOnNewSetItems() {
        SearchableListModel<Void, Integer> listModel =
                mock(SearchableListModel.class, withSettings().useConstructor().defaultAnswer(Answers.CALLS_REAL_METHODS));
        listModel.setItems(Arrays.asList(1, 2, 3));
        listModel.setSelectedItem(2);

        listModel.setItems(Arrays.asList(1, 2));

        assertEquals((Integer) 2, listModel.getSelectedItem());
    }
}
