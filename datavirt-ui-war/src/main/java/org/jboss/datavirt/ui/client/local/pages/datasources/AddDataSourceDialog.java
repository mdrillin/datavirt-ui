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
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Dialog that allows the user to add a new data source
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/dialogs/add-datasource-dialog.html#add-datasource-dialog")
@Dependent
public class AddDataSourceDialog extends ModalDialog implements HasValueChangeHandlers<DataSourceDetailsBean> {

	private static final String NO_SELECTION = "[Select a Type]";
	
    @Inject
    protected ClientMessages i18n;
    @Inject @DataField("add-datasource-status-label")
    protected Label statusLabel;
    @Inject @DataField("add-datasource-name-textbox")
    protected TextBox dsName;
    @Inject @DataField("add-datasource-type-listbox")
    protected ListBox sourceTypeListBox;
    @Inject @DataField("add-datasource-core-properties-table")
    protected DataSourcePropertiesTable dataSourceCorePropertiesTable;
    @Inject @DataField("add-datasource-advanced-properties-table")
    protected DataSourcePropertiesTable dataSourceAdvancedPropertiesTable;
    @Inject @DataField("add-datasource-submit-button")
    protected Button submitButton;

    @Inject
    protected DataSourceRpcService dataSourceService;

    private Collection<String> currentDsNames;
    private Collection<String> requiredPropNames = new ArrayList<String>();
    
    /**
     * Constructor.
     */
    public AddDataSourceDialog( ) {
    }
    
    /**
     * Constructor.
     */
    public AddDataSourceDialog(Collection<String> currentDsNames) {
    	this.currentDsNames = currentDsNames;
    }
    
    public void setCurrentDsNames(Collection<String> currentDsNames) {
    	this.currentDsNames = currentDsNames;
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
        
        dataSourceCorePropertiesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
                //doDataSourceSearch(currentPage);
            }
        });
        dataSourceAdvancedPropertiesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
                //doDataSourceSearch(currentPage);
            }
        });
        
        dsName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	updateDialogStatus();
            }
        });
        // Change Listener for Type ListBox
        sourceTypeListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String selectedType = getDialogSourceType();                                
        		doPopulatePropertiesTable(selectedType);
        	}
        });
        
    }

    /**
     * Populate the Data Source Type ListBox
     */
    protected void doPopulateSourceTypeListBox() {
//		final String searchText = this.searchBox.getValue();
//        final SortColumn currentSortColumn = this.dataSourcesTable.getCurrentSortColumn();

//        stateService.put(ApplicationStateKeys.DATASOURCES_SEARCH_TEXT, searchText);
//        stateService.put(ApplicationStateKeys.DATASOURCES_PAGE, currentPage);
//        stateService.put(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, currentSortColumn);
        
        dataSourceService.getDataSourceTypes(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> dsTypes) {
                populateSourceTypeListBox(dsTypes);
                //updatePager(data);
                //doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                //notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
                //noDataMessage.setVisible(true);
                //searchInProgressMessage.setVisible(false);
            }
        });

    }

    /*
     * Init the List of DataSource Template Names
     * @param vdbName the name of the VDB
     * @param sourceName the source name
     * @param templateName the template name
     * @param translatorName the translator name
     * @param propsMap the property Map of name-value pairs
     */
    private void populateSourceTypeListBox(List<String> sourceTypes) {
    	// Make sure clear first
    	sourceTypeListBox.clear();

    	sourceTypeListBox.insertItem(NO_SELECTION, 0);
    	
    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String typeName: sourceTypes) {
    		sourceTypeListBox.insertItem(typeName, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	sourceTypeListBox.setSelectedIndex(0);
    }
    
    /**
     * Populate the properties table for the supplied Source Type
     * @param selectedType the selected SourceType
     */
    protected void doPopulatePropertiesTable(String selectedType) {
//		final String searchText = this.searchBox.getValue();
//        final SortColumn currentSortColumn = this.dataSourcesTable.getCurrentSortColumn();

//        stateService.put(ApplicationStateKeys.DATASOURCES_SEARCH_TEXT, searchText);
//        stateService.put(ApplicationStateKeys.DATASOURCES_PAGE, currentPage);
//        stateService.put(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, currentSortColumn);
        if(selectedType.equals(NO_SELECTION)) {
        	dataSourceCorePropertiesTable.clear();
        	dataSourceAdvancedPropertiesTable.clear();
        	return;
        }

        dataSourceService.getDataSourceTypeProperties(selectedType, new IRpcServiceInvocationHandler<List<DataSourcePropertyBean>>() {
            @Override
            public void onReturn(List<DataSourcePropertyBean> propList) {
                populatePropertiesTable(propList);
                updateDialogStatus();
            }
            @Override
            public void onError(Throwable error) {
                //notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
                //noDataMessage.setVisible(true);
                //searchInProgressMessage.setVisible(false);
            }
        });

    }
    
    private void populatePropertiesTable(List<DataSourcePropertyBean> propList) {
    	this.requiredPropNames.clear();
    	
    	dataSourceCorePropertiesTable.clear();
    	dataSourceAdvancedPropertiesTable.clear();
    	
    	for(DataSourcePropertyBean defn : propList) {
    		if(defn.isCoreProperty()) {
    			dataSourceCorePropertiesTable.addRow(defn);
    		} else {
    			dataSourceAdvancedPropertiesTable.addRow(defn);
    		}
    		if(defn.isRequired()) this.requiredPropNames.add(defn.getDisplayName());
    	}
    	dataSourceCorePropertiesTable.setVisible(true);
    	dataSourceAdvancedPropertiesTable.setVisible(true);
    }
    
    /**
     * @see org.jboss.datavirt.ui.client.local.widgets.bootstrap.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();
        dsName.setFocus(true);
		statusLabel.setText(i18n.format("addDataSourceDialog.statusSelectDSType"));
        
        doPopulateSourceTypeListBox();
        
        //SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, dataSourcesTable.getDefaultSortColumn());

    	this.dataSourceCorePropertiesTable.sortBy("name", true);
    	this.dataSourceAdvancedPropertiesTable.sortBy("name", true);
    	//String selectedType = getDialogSourceType();
    	//doPopulatePropertiesTable(selectedType);
    }

    private void updateDialogStatus() {
    	boolean isValid = validateSourceProperties();
    	submitButton.setEnabled(isValid);
    }
    
    /*
     * Validate the entered properties and return status. The status message label is also updated.
     * @return the property validation status. 'true' if properties are valid, 'false' otherwise.
     */
    private boolean validateSourceProperties( ) {
    	boolean isValid = true;
    	String statusStr = "OK";

    	// Make sure source type is selected
		String selectedType = getDialogSourceType();                                
    	if(NO_SELECTION.equals(selectedType)) {
    		statusStr = i18n.format("addDataSourceDialog.statusSelectDSType");
    		isValid = false;
    	}
    	
    	// Validate the entered name
    	String dsNameStr = dsName.getText();
    	if(isValid) {
    		if(dsNameStr==null || dsNameStr.trim().length()==0) {
    			statusStr = i18n.format("addDataSourceDialog.statusEnterDSName");
    			isValid = false;
    		}
    	}
    	
    	// Check entered name against existing names
    	if(isValid) {
    		if(currentDsNames.contains(dsNameStr)) {
        		statusStr = i18n.format("addDataSourceDialog.statusDSNameAlreadyExists");
        		isValid = false;
    		}
    	}

    	// Validate the Properties Tables
    	if(isValid) {
    		// Temp set the Property status to OK
    		statusStr = "OK";
//    		int coreRowCount = this.dataSourceCorePropertiesTable.getRowCount();
//    		for(int i=0 ; i<coreRowCount; i++) {
//    			this.dataSourceCorePropertiesTable.getRow(rowIndex);
//    		}
    		
    		if(!statusStr.equals("OK")) {
    			isValid = false;
    		} 
    	}
    	
    	// Update the status label
    	if(!statusStr.equals("OK")) {
    		statusLabel.setText(statusStr);
    	} else {
    		statusLabel.setText(i18n.format("addDataSourceDialog.statusClickOkToAccept"));
    	}

    	return isValid;
    }
    
    private String getDialogSourceType() {
    	int index = sourceTypeListBox.getSelectedIndex();
    	return sourceTypeListBox.getValue(index);
    }
    
    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("add-datasource-submit-button")
    protected void onSubmit(ClickEvent event) {
    	DataSourceDetailsBean resultBean = new DataSourceDetailsBean();
    	resultBean.setName(dsName.getText());
    	resultBean.setType(getDialogSourceType());
    	
    	List<DataSourcePropertyBean> props = new ArrayList<DataSourcePropertyBean>();
    	List<DataSourcePropertyBean> coreProps = dataSourceCorePropertiesTable.getBeansWithRequiredOrNonDefaultValue();
    	List<DataSourcePropertyBean> advancedProps = dataSourceAdvancedPropertiesTable.getBeansWithRequiredOrNonDefaultValue();
    	props.addAll(coreProps);
    	props.addAll(advancedProps);
    	
    	resultBean.setProperties(props);
    	
        ValueChangeEvent.fire(this, resultBean);

        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DataSourceDetailsBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
