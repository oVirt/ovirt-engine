package org.ovirt.engine.ui.uicommonweb.models;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.Silent;
import org.ovirt.engine.ui.uicommonweb.junit.UiCommonSetup;

@RunWith(Silent.class)
public class SearchableListModelTest {

    @ClassRule
    public static UiCommonSetup setup = new UiCommonSetup();

    @Spy
    private SearchableListModel<Void, Integer> listModel;

    @Test
    public void testSelectionRestoredOnNewSetItems() {
        listModel.setItems(Arrays.asList(1, 2, 3));
        listModel.setSelectedItem(2);

        listModel.setItems(Arrays.asList(1, 2));

        assertEquals((Integer) 2, listModel.getSelectedItem());
    }
}
