package org.jboss.datavirt.ui.client.local.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Event fired by the sortable table widget when the user clicks on one of the
 * table headers (in order to specify a sort order).
 *
 * @author eric.wittmann@redhat.com
 */
public class TableRowSelectionEvent extends GwtEvent<TableRowSelectionEvent.Handler> {

	/**
	 * Handler for {@link TableRowSelectionEvent}.
	 */
	public static interface Handler extends EventHandler {

		/**
		 * Called when {@link TableRowSelectionEvent} is fired.
		 *
		 * @param event the {@link TableRowSelectionEvent} that was fired
		 */
		public void onTableRowSelection(TableRowSelectionEvent event);
	}

	/**
	 * Indicates if a widget supports ok/cancel.
	 */
	public static interface HasTableRowSelectionHandlers extends HasHandlers {

		/**
		 * Adds a handler to the widget.
		 * @param handler
		 */
		public HandlerRegistration addTableRowSelectionHandler(Handler handler);

	}

	private static Type<Handler> TYPE;

	/**
	 * Fires the event.
	 *
	 * @param source
	 * @param columnId
	 * @param ascending
	 */
	public static TableRowSelectionEvent fire(HasTableRowSelectionHandlers source, int numberRowsSelected) {
		TableRowSelectionEvent event = new TableRowSelectionEvent(numberRowsSelected);
		if (TYPE != null)
			source.fireEvent(event);
		return event;
	}

	/**
	 * Gets the type associated with this event.
	 *
	 * @return returns the handler type
	 */
	public static Type<Handler> getType() {
		if (TYPE == null) {
			TYPE = new Type<Handler>();
		}
		return TYPE;
	}

	private int nRowsSelected = 0;

	/**
	 * Constructor.
	 * @param columnId
	 * @param ascending
	 */
	public TableRowSelectionEvent(int nSelected) {
	    this.nRowsSelected = nSelected;
	}

	/**
	 * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
	 */
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}
	
	public int getNumberRowsSelected() {
		return nRowsSelected;
	}

	/**
	 * @see com.google.gwt.event.shared.GwtEvent#dispatch(com.google.gwt.event.shared.EventHandler)
	 */
	@Override
	protected void dispatch(Handler handler) {
		handler.onTableRowSelection(this);
	}

}
