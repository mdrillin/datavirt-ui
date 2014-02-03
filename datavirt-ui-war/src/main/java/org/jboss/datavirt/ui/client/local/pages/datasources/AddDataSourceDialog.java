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
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.beans.PropertyBeanComparator;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable.SortColumn;

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

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected NotificationService notificationService;
    
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
    private List<DataSourcePropertyBean> currentPropList = new ArrayList<DataSourcePropertyBean>();
    
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
        
    	dataSourceCorePropertiesTable.addValueChangeHandler(new ValueChangeHandler<Void>() {
            @Override
            public void onValueChange(ValueChangeEvent<Void> event) {
            	updateDialogStatus();
            }
        });
    	dataSourceAdvancedPropertiesTable.addValueChangeHandler(new ValueChangeHandler<Void>() {
            @Override
            public void onValueChange(ValueChangeEvent<Void> event) {
            	updateDialogStatus();
            }
        });
    	dataSourceCorePropertiesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
            	populateCorePropertiesTable();
            }
        });
    	dataSourceAdvancedPropertiesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
            	populateAdvancedPropertiesTable();
            }
        });
        
    }

    /**
     * Populate the Data Source Type ListBox
     */
    protected void doPopulateSourceTypeListBox() {
        dataSourceService.getDataSourceTypes(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> dsTypes) {
                populateSourceTypeListBox(dsTypes);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("adddatasourcedialog.error-populating-types-listbox"), error); //$NON-NLS-1$
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

    	sourceTypeListBox.insertItem(Constants.NO_TYPE_SELECTION, 0);
    	
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
        if(selectedType.equals(Constants.NO_TYPE_SELECTION)) {
        	dataSourceCorePropertiesTable.clear();
        	dataSourceAdvancedPropertiesTable.clear();
        	return;
        }

        dataSourceService.getDataSourceTypeProperties(selectedType, new IRpcServiceInvocationHandler<List<DataSourcePropertyBean>>() {
            @Override
            public void onReturn(List<DataSourcePropertyBean> propList) {
            	currentPropList.clear();
            	currentPropList.addAll(propList);
                populateCorePropertiesTable();
                populateAdvancedPropertiesTable();
                updateDialogStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("adddatasourcedialog.error-populating-properties-table"), error); //$NON-NLS-1$
            }
        });

    }
    
    /*
     * Populate the core properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateCorePropertiesTable( ) {
    	dataSourceCorePropertiesTable.clear();

        final SortColumn currentSortColumnCore = this.dataSourceCorePropertiesTable.getCurrentSortColumn();

    	// Separate property types, sorted in correct order
    	List<DataSourcePropertyBean> corePropList = getPropList(this.currentPropList, true, !currentSortColumnCore.ascending);
    	// Populate core properties table
    	for(DataSourcePropertyBean defn : corePropList) {
    		dataSourceCorePropertiesTable.addRow(defn);
    	}
    	dataSourceCorePropertiesTable.setValueColTextBoxWidths();
    	dataSourceCorePropertiesTable.setVisible(true);
    }

    /*
     * Populate the advanced properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateAdvancedPropertiesTable() {
    	dataSourceAdvancedPropertiesTable.clear();

        final SortColumn currentSortColumnAdv = this.dataSourceAdvancedPropertiesTable.getCurrentSortColumn();

    	// Separate property types, sorted in correct order
    	List<DataSourcePropertyBean> advPropList = getPropList(this.currentPropList, false, !currentSortColumnAdv.ascending);
    	// Populate advanced properties table
    	for(DataSourcePropertyBean defn : advPropList) {
    		dataSourceAdvancedPropertiesTable.addRow(defn);
    	}
    	dataSourceAdvancedPropertiesTable.setValueColTextBoxWidths();
    	dataSourceAdvancedPropertiesTable.setVisible(true);
    }
    
    /*
     * Filters the supplied list by correct type and order
     * @param propList the complete list of properties
     * @param getCore if 'true', returns the core properties.  if 'false' returns the advanced properties
     * @param acending if 'true', sorts in ascending name order.  descending if 'false'
     */
    private List<DataSourcePropertyBean> getPropList(List<DataSourcePropertyBean> propList, boolean getCore, boolean ascending) {
    	List<DataSourcePropertyBean> resultList = new ArrayList<DataSourcePropertyBean>();
    	
    	// Put only the desired property type into the resultList
    	for(DataSourcePropertyBean prop : propList) {
    		if(prop.isCoreProperty() && getCore) {
    			resultList.add(prop);
    		} else if(!prop.isCoreProperty() && !getCore) {
    			resultList.add(prop);    			
    		}
    	}
    	
    	// Sort by name in the desired order
    	Collections.sort(resultList,new PropertyBeanComparator(ascending));
    	
    	return resultList;
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
        
        SortColumn sortColumnCore = dataSourceCorePropertiesTable.getDefaultSortColumn();
        SortColumn sortColumnAdv = dataSourceAdvancedPropertiesTable.getDefaultSortColumn();

    	this.dataSourceCorePropertiesTable.sortBy(sortColumnCore.columnId, sortColumnCore.ascending);
    	this.dataSourceAdvancedPropertiesTable.sortBy(sortColumnAdv.columnId, sortColumnAdv.ascending);
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
    	String statusStr = Constants.OK;

    	// Make sure source type is selected
		String selectedType = getDialogSourceType();                                
    	if(Constants.NO_TYPE_SELECTION.equals(selectedType)) {
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
        	statusStr = this.dataSourceCorePropertiesTable.getStatus();
        	if(statusStr.equalsIgnoreCase(Constants.OK)) {
        		statusStr = this.dataSourceAdvancedPropertiesTable.getStatus();
        	}
    		
    		if(!statusStr.equals(Constants.OK)) {
    			isValid = false;
    		} 
    	}
    	
    	// Update the status label
    	if(!statusStr.equals(Constants.OK)) {
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
