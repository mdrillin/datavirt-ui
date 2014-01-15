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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;

import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.services.StringUtil;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A table of Data Source properties.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
public class DataSourcePropertiesTable extends SortableTemplatedWidgetTable {

    private Map<Integer,String> rowNameMap = new HashMap<Integer,String>();
    private Map<Integer,DataSourcePropertyBean> rowBeanMap = new HashMap<Integer,DataSourcePropertyBean>();

    /**
     * Constructor.
     */
    public DataSourcePropertiesTable() {
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
        setColumnSortable(0, Constants.SORT_COLID_NAME);
        sortBy(Constants.SORT_COLID_NAME, true);
    }

    public void clear() {
    	super.clear();
    	rowBeanMap.clear();
    	rowNameMap.clear();
    }
    
    /**
     * Adds a single row to the table.
     * @param dataSourcePropertyBean
     */
    public void addRow(final DataSourcePropertyBean dataSourcePropertyBean) {
        int rowIdx = this.rowElements.size();

        InlineLabel name = new InlineLabel(dataSourcePropertyBean.getName());
        
        TextBox valueTextBox = new TextBox();
        valueTextBox.setText(dataSourcePropertyBean.getValue());
        
        valueTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                int rowIdx = rowElements.size();
                String value = getText();
                DataSourcePropertyBean propBean = rowBeanMap.get(rowIdx);
                propBean.setValue(value);
            }
        });
        valueTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                int rowIdx = rowElements.size();
                String value = getText();
                DataSourcePropertyBean propBean = rowBeanMap.get(rowIdx);
                propBean.setValue(value);
            }
        });

        add(rowIdx, 0, name);
        add(rowIdx, 1, valueTextBox);
        
        rowNameMap.put(rowIdx,dataSourcePropertyBean.getName());
        rowBeanMap.put(rowIdx,dataSourcePropertyBean);
    }
    
    public List<DataSourcePropertyBean> getBeansWhereValueNotDefault() {
    	List<DataSourcePropertyBean> resultBeans = new ArrayList<DataSourcePropertyBean>();
    	for(DataSourcePropertyBean propBean : rowBeanMap.values()) {
    		String defaultValue = propBean.getDefaultValue();
    		String value = propBean.getValue();
    		if(!StringUtil.valuesAreEqual(value, defaultValue)) {
    			resultBeans.add(propBean);
    		}
    	}
    	return resultBeans;
    }
        
}
