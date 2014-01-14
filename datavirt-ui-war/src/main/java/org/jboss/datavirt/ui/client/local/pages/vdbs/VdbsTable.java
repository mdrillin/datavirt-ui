/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.datavirt.ui.client.local.pages.vdbs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.Handler;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.HasTableRowSelectionHandlers;
import org.jboss.datavirt.ui.client.local.pages.VdbDetailsPage;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.VdbSummaryBean;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of VDBs.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
public class VdbsTable extends SortableTemplatedWidgetTable implements HasTableRowSelectionHandlers {

    @Inject
    protected TransitionAnchorFactory<VdbDetailsPage> toDetailsPageLinkFactory;

    private Map<Integer,CheckBox> rowSelectionMap = new HashMap<Integer,CheckBox>();
    private Map<Integer,String> rowNameMap = new HashMap<Integer,String>();

    /**
     * Constructor.
     */
    public VdbsTable() {
    }

    /**
     * @see org.jboss.datavirt.ui.client.local.widgets.common.SortableTemplatedWidgetTable#getDefaultSortColumn()
     */
    @Override
    public SortColumn getDefaultSortColumn() {
        SortColumn sortColumn = new SortColumn();
        sortColumn.columnId = Constants.SORT_COLID_NAME;
        sortColumn.ascending = true;
        return sortColumn;
    }

    /**
     * @see org.overlord.monitoring.ui.client.local.widgets.common.SortableTemplatedWidgetTable#configureColumnSorting()
     */
    @Override
    protected void configureColumnSorting() {
        setColumnSortable(1, Constants.SORT_COLID_NAME);
        setColumnSortable(4, Constants.SORT_COLID_MODIFIED_ON);
        sortBy(Constants.SORT_COLID_NAME, true);
    }

    /**
     * Adds a single row to the table.
     * @param dataSourceSummaryBean
     */
    public void addRow(final VdbSummaryBean vdbSummaryBean) {
        int rowIdx = this.rowElements.size();
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy"); //$NON-NLS-1$

        CheckBox checkbox = new CheckBox();
        checkbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	TableRowSelectionEvent.fire(VdbsTable.this, getSelectedVdbs().size());
            }
        });
        
        add(rowIdx,0,checkbox);
        rowSelectionMap.put(rowIdx,checkbox);
        rowNameMap.put(rowIdx,vdbSummaryBean.getName());
        
        Anchor name = toDetailsPageLinkFactory.get("vdbname", vdbSummaryBean.getName()); //$NON-NLS-1$
        name.setText(vdbSummaryBean.getName());
        InlineLabel type = new InlineLabel(vdbSummaryBean.getType());
        InlineLabel status = new InlineLabel(vdbSummaryBean.getStatus());
        InlineLabel modified = new InlineLabel(format.format(vdbSummaryBean.getUpdatedOn()));

        add(rowIdx, 1, name);
        add(rowIdx, 2, type);
        add(rowIdx, 3, status);
        add(rowIdx, 4, modified);
    }
    
    public HandlerRegistration addTableRowSelectionHandler(Handler handler) {
        return addHandler(handler, TableRowSelectionEvent.getType());
    }

    public boolean isSelected(int rowIndex) {
    	CheckBox chkbox = rowSelectionMap.get(rowIndex);
    	return chkbox.getValue();
    }

    public Collection<String> getSelectedVdbs() {
    	Collection<String> selectedDataSources = new ArrayList<String>();
    	for(Integer row : rowSelectionMap.keySet()) {
    		if(isSelected(row)) {
    			selectedDataSources.add(rowNameMap.get(row));
    		}
    	}
    	return selectedDataSources;
    }

}
