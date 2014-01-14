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
package org.jboss.datavirt.ui.client.local.pages.datasources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;

import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.Handler;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.HasTableRowSelectionHandlers;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeBean;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of Data Source Types.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
public class DataSourceTypesTable extends SortableTemplatedWidgetTable implements HasTableRowSelectionHandlers {

    private Map<Integer,CheckBox> rowSelectionMap = new HashMap<Integer,CheckBox>();
    private Map<Integer,String> rowNameMap = new HashMap<Integer,String>();

    private List<String> builtInDrivers = Arrays.asList("file","google","h2","infinispan","ldap","modeshape","mongodb","salesforce","teiid","teiid-local","webservice");
    
    /**
     * Constructor.
     */
    public DataSourceTypesTable() {
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
        setColumnSortable(2, Constants.SORT_COLID_MODIFIED_ON);
        sortBy(Constants.SORT_COLID_NAME, true);
    }

    public void clear() {
    	super.clear();
    	rowSelectionMap.clear();
    	rowNameMap.clear();
    }
    
    /**
     * Adds a single row to the table.
     * @param dataSourceSummaryBean
     */
    public void addRow(final DataSourceTypeBean dataSourceTypeBean) {
        int rowIdx = this.rowElements.size();
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy"); //$NON-NLS-1$

        String dsTypeName = dataSourceTypeBean.getName();
        if(!builtInDrivers.contains(dsTypeName)) {
            CheckBox checkbox = new CheckBox();
            checkbox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                	TableRowSelectionEvent.fire(DataSourceTypesTable.this, getSelectedDataSourceTypes().size());
                }
            });
            
            add(rowIdx,0,checkbox);
            rowSelectionMap.put(rowIdx,checkbox);
            rowNameMap.put(rowIdx,dataSourceTypeBean.getName());
        } else {
        	add(rowIdx,0,new InlineLabel(""));
        }
        
        InlineLabel name = new InlineLabel(dsTypeName);
        InlineLabel modified = new InlineLabel(format.format(dataSourceTypeBean.getUpdatedOn()));

        add(rowIdx, 1, name);
        add(rowIdx, 2, modified);
    }
    
    /**
     * @see org.overlord.sramp.ui.client.local.events.TableSortEvent.HasTableSortHandlers#addTableSortHandler(org.overlord.sramp.ui.client.local.events.TableSortEvent.Handler)
     */
    public HandlerRegistration addTableRowSelectionHandler(Handler handler) {
        return addHandler(handler, TableRowSelectionEvent.getType());
    }

    public boolean isSelected(int rowIndex) {
    	CheckBox chkbox = rowSelectionMap.get(rowIndex);
    	return chkbox.getValue();
    }
    
    public Collection<String> getSelectedDataSourceTypes() {
    	Collection<String> selectedDataSourceTypes = new ArrayList<String>();
    	for(Integer row : rowSelectionMap.keySet()) {
    		if(isSelected(row)) {
    			selectedDataSourceTypes.add(rowNameMap.get(row));
    		}
    	}
    	return selectedDataSourceTypes;
    }

}
