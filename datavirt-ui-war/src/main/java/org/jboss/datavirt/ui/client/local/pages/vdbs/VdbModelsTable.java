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

import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.Handler;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent.HasTableRowSelectionHandlers;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.VdbModelBean;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of VDB Models.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
public class VdbModelsTable extends SortableTemplatedWidgetTable implements HasTableRowSelectionHandlers {

    private Map<Integer,CheckBox> rowSelectionMap = new HashMap<Integer,CheckBox>();
    private Map<Integer,String> rowNameMap = new HashMap<Integer,String>();
    private Map<Integer,String> rowTypeMap = new HashMap<Integer,String>();
    private Map<Integer,String> rowTranslatorMap = new HashMap<Integer,String>();

    /**
     * Constructor.
     */
    public VdbModelsTable() {
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
        sortBy(Constants.SORT_COLID_NAME, true);
    }

    public void clear() {
    	super.clear();
        rowSelectionMap.clear();
        rowNameMap.clear();
        rowTypeMap.clear();
        rowTranslatorMap.clear();
    }
    
    /**
     * Adds a single row to the table.
     * @param dataSourceSummaryBean
     */
    public void addRow(final VdbModelBean vdbModelBean) {
        int rowIdx = this.rowElements.size();

        CheckBox checkbox = new CheckBox();
        checkbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	TableRowSelectionEvent.fire(VdbModelsTable.this, getSelectedModelNameAndTypeMap().size());
            }
        });
        
        add(rowIdx,0,checkbox);
        rowSelectionMap.put(rowIdx,checkbox);
        rowNameMap.put(rowIdx,vdbModelBean.getName());
        rowTypeMap.put(rowIdx,vdbModelBean.getType());
        rowTranslatorMap.put(rowIdx,vdbModelBean.getTranslator());
        
        InlineLabel name = new InlineLabel(vdbModelBean.getName());
        InlineLabel status = new InlineLabel(vdbModelBean.getStatus());
        InlineLabel modelType = new InlineLabel(vdbModelBean.getType());
        InlineLabel translator = new InlineLabel(vdbModelBean.getTranslator());
        InlineLabel jndiSource = new InlineLabel(vdbModelBean.getJndiSource());

        add(rowIdx, 1, name);
        add(rowIdx, 2, status);
        add(rowIdx, 3, modelType);
        add(rowIdx, 4, translator);
        add(rowIdx, 5, jndiSource);
    }
    
    public HandlerRegistration addTableRowSelectionHandler(Handler handler) {
        return addHandler(handler, TableRowSelectionEvent.getType());
    }

    public boolean isSelected(int rowIndex) {
    	CheckBox chkbox = rowSelectionMap.get(rowIndex);
    	return chkbox.getValue();
    }

    public Map<String,String> getSelectedModelNameAndTypeMap() {
    	Map<String,String> selectedModelAndTypeMap = new HashMap<String,String>();
    	for(Integer row : rowSelectionMap.keySet()) {
    		if(isSelected(row)) {
    			selectedModelAndTypeMap.put(rowNameMap.get(row),rowTypeMap.get(row));
    		}
    	}
    	return selectedModelAndTypeMap;
    }

    public Map<String,String> getSelectedModelNameAndTranslatorMap() {
    	Map<String,String> selectedModelAndTranslatorMap = new HashMap<String,String>();
    	for(Integer row : rowSelectionMap.keySet()) {
    		if(isSelected(row)) {
    			selectedModelAndTranslatorMap.put(rowNameMap.get(row),rowTranslatorMap.get(row));
    		}
    	}
    	return selectedModelAndTranslatorMap;
    }
    
}
