package org.ovirt.engine.ui.userportal.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * @author Asaf Shakarchi An abstract SmartGWT Datasource for GWT-RPC protocol
 */
public abstract class RPCDataSource<T> extends DataSource {
	private List<String> hightlightingFieldNames = new ArrayList<String>();

	public RPCDataSource() {
		this(null);
	}

	public RPCDataSource(String name) {
		if (name != null) {
			System.out.println("Trying to build DS: " + name);
			setID(name);
		}
		setClientOnly(false);
		setAutoCacheAllData(false);
		setCacheAllData(false);
		setDataProtocol(DSProtocol.CLIENTCUSTOM);
		setDataFormat(DSDataFormat.CUSTOM);
	}

	@Override
	protected Object transformRequest(DSRequest request) {
		try {
			DSResponse response = createResponse(request);

			switch (request.getOperationType()) {
			case FETCH:
				executeFetch(request, response);
				break;
			case ADD:
				executeAdd(request, response);
				break;
			case UPDATE:
				executeUpdate(request, response);
				break;
			case REMOVE:
				executeRemove(request, response);
				break;
			default:
				super.transformRequest(request);
				break;
			}
		} catch (Throwable t) {
			// CoreGUI.getErrorHandler().handleError("Failure in datasource [" +
			// request.getOperationType() + "]", t);
			return null;
		}
		return request.getData();
	}

	public ListGridRecord[] buildRecords(Collection<T> list) {
		if (list == null) {
			return null;
		}

		ListGridRecord[] records = new ListGridRecord[list.size()];
		int i = 0;
		for (T item : list) {
			records[i++] = copyValues(item);
		}
		return records;
	}

	@Override
	public void addField(DataSourceField field) throws IllegalStateException {
		super.addField(field);
		field.setHidden(true);

		hightlightingFieldNames.add(field.getName());

		String name = field.getName() + "-highlight";
		String title = field.getTitle();
		DataSourceTextField fieldToDisplayHighlighting = new DataSourceTextField(
				name, title);
		super.addField(fieldToDisplayHighlighting);
	}

	protected void highlightFilterMatches(final DSRequest request,
			final ListGridRecord[] records) {
		// Map<String, Object> criteriaMap = request.getCriteria().getValues();
		//
		// for (String filterName : hightlightingFieldNames) {
		// String filterValue = (String) criteriaMap.get(filterName);
		// for (ListGridRecord nextRecord : records) {
		// String originalData = nextRecord.getAttribute(filterName);
		// // String decoratedData = (filterValue != null) ?
		// ColoringUtility.highlight(originalData, filterValue)
		// // : originalData;
		// // nextRecord.setAttribute(filterName + "-highlight", decoratedData);
		// }
		// }
	}

	/**
	 * Extensions should implement this method to retrieve data. Paging
	 * solutions should use
	 * {@link #getPageControl(com.smartgwt.client.data.DSRequest)}. All
	 * implementations should call processResponse() whether they fail or
	 * succeed. Data should be set on the request via setData. Implementations
	 * can use buildRecords() to get the list of records.
	 * 
	 * @param request
	 * @param response
	 */
	protected abstract void executeFetch(final DSRequest request,
			final DSResponse response);

	public abstract T copyValues(ListGridRecord from);

	public abstract ListGridRecord copyValues(T from);

	/**
	 * Executed on <code>REMOVE</code> operation.
	 * <code>processResponse (requestId, response)</code> should be called when
	 * operation completes (either successful or failure).
	 * 
	 * @param request
	 *            <code>DSRequest</code> being processed.
	 *            <code>request.getData ()</code> contains record should be
	 *            removed.
	 * @param response
	 *            <code>DSResponse</code>. <code>setData (list)</code> should be
	 *            called on successful execution of this method. Array should
	 *            contain single element representing removed row.
	 *            <code>setStatus (&lt;0)</code> should be called on failure.
	 */
	protected void executeRemove(final DSRequest request,
			final DSResponse response) {
		throw new UnsupportedOperationException(
				"This dataSource does not support removal.");
	}

	protected void executeAdd(final DSRequest request, final DSResponse response) {
		throw new UnsupportedOperationException(
				"This dataSource does not support addition.");
	}

	protected void executeUpdate(final DSRequest request,
			final DSResponse response) {
		throw new UnsupportedOperationException(
				"This dataSource does not support updates.");
	}

	private DSResponse createResponse(DSRequest request) {
		DSResponse response = new DSResponse();
		response.setAttribute("clientContext",
				request.getAttributeAsObject("clientContext"));
		// Assume success as the default.
		response.setStatus(0);
		return response;
	}

	/**
	 * Add the specified fields to this data source. When the data source is
	 * associated with a {@link com.smartgwt.client.widgets.grid.ListGrid}, the
	 * fields will be displayed in the order they are specified here.
	 * 
	 * @param fields
	 *            the fields to be added
	 */
	public void addFields(List<DataSourceField> fields) {
		for (DataSourceField field : fields) {
			addField(field);
		}
	}
}
