package org.ovirt.engine.ui.userportal.client.components;

import java.util.ArrayList;
import java.util.HashMap;

import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.viewer.DetailViewer;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

public class SubTabDetailViewer extends HLayout {
	private final static int maxRows = 5;
	private ArrayList<DetailViewer> viewers;
	private HashMap<String,DetailViewerField> viewerFields = new HashMap<String,DetailViewerField>();
	
	public SubTabDetailViewer(DetailViewerField[] fields, Integer[] numOfRowsInColumn) {
		setWidth100();
		setHeight100();
		setOverflow(Overflow.AUTO);
		setMembersMargin(30);
		setPadding(5);
		
		// If no column-sizes were specified, regressing to regular mode (based on maxRows)
		if (numOfRowsInColumn.length == 0)
		{
			int columns = (fields.length / maxRows) + 1;
			numOfRowsInColumn = new Integer[columns];
			for (int i = 0; i < columns; i++) 
				numOfRowsInColumn[i] = maxRows;			
		}
		
		viewers = new ArrayList<DetailViewer>(numOfRowsInColumn.length);		
		int fieldCounter = 0;
		for (int i=0; i<numOfRowsInColumn.length; i++) {
			DetailViewerField[] currFields = new DetailViewerField[numOfRowsInColumn[i]];
			for (int j=0; j<numOfRowsInColumn[i]; j++) {				
				if (fieldCounter == fields.length)
					break;
				viewerFields.put(fields[fieldCounter].getName(),fields[fieldCounter]);
				currFields[j] = fields[fieldCounter++];
			}
			DetailViewer viewer = new DetailViewer();
			viewer.setShowEmptyMessage(false);
			viewer.setWrapValues(false);
			viewer.setFields(currFields);
			viewers.add(viewer);
			addMember(viewer);
		}		
	}

	public void setData(RecordList data) {
		for (String fieldName : viewerFields.keySet())
		{	
			boolean isFieldHidden = data.get(0).getAttributeAsBoolean(fieldName + "_hidden");
			viewerFields.get(fieldName).setAttribute("hidden", isFieldHidden);				
		}
		
		for (DetailViewer viewer : viewers) {
			
			viewer.setData(data);
		}
	}

}
