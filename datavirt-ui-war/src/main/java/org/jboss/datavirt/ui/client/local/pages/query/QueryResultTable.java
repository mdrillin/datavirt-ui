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
package org.jboss.datavirt.ui.client.local.pages.query;

import java.util.List;

import javax.enterprise.context.Dependent;

import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.Handler;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.HasTableRowSelectionHandlers;
import org.jboss.datavirt.ui.client.shared.beans.QueryResultRowBean;
import org.overlord.sramp.ui.client.local.widgets.common.TemplatedWidgetTable;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of the Query Columns.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
public class QueryResultTable extends TemplatedWidgetTable implements HasTableRowSelectionHandlers {

    /**
     * Constructor.
     */
    public QueryResultTable() {
    }

    public void clear() {
    	super.clear();
    }
    
    /**
     * Adds a single row to the table.
     * @param queryColumnBean
     */
    public void addRow(final QueryResultRowBean queryResultRowBean) {
        int rowIdx = this.rowElements.size();
        
        List<String> rowValues = queryResultRowBean.getColumnResults();
//        int nCols = rowValues.size();
        int nCols = 2;
        for(int i=0 ; i<nCols; i++) {
        	String value = rowValues.get(i);
            InlineLabel valueLabel = new InlineLabel(value);
            add(rowIdx,i,valueLabel);
        }
    }
    
    /**
     * @see org.overlord.sramp.ui.client.local.events.TableSortEvent.HasTableSortHandlers#addTableSortHandler(org.overlord.sramp.ui.client.local.events.TableSortEvent.Handler)
     */
    public HandlerRegistration addTableRowSelectionHandler(Handler handler) {
        return addHandler(handler, TableRowSelectionEvent.getType());
    }

}
