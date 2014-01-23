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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.services.StringUtils;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
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
 * Dialog that allows the user to add a Source Model to a VDB
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/dialogs/add-source-model-dialog.html#add-source-model-dialog")
@Dependent
public class AddSourceModelDialog extends ModalDialog implements HasValueChangeHandlers<Map<String, String>> {

    @Inject
    protected ClientMessages i18n;

    @Inject
    protected NotificationService notificationService;

    @Inject @DataField("addsourcemodel-status-label")
    protected Label statusLabel;
    
    @Inject @DataField("addsourcemodel-name-textbox")
    protected TextBox modelNameTextBox;
    
    @Inject @DataField("addsourcemodel-datasource-listbox")
    protected ListBox dataSourceListBox;

    @Inject @DataField("addsourcemodel-translator-listbox")
    protected ListBox translatorListBox;
    
    @Inject @DataField("add-source-model-submit-button")
    protected Button submitButton;

    @Inject
    protected DataSourceRpcService dataSourceService;

    private static final String NO_SELECTION = "[No Selection]";
    
    private Map<String,String> defaultTranslatorMap;
    private Collection<String> currentModelNames = Collections.emptyList();
    
    /**
     * Constructor.
     */
    public AddSourceModelDialog() {
    }

    public void setCurrentModelNames(Collection<String> currentModelNames) {
    	this.currentModelNames = currentModelNames;
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);

        // Change Listener for DataSource ListBox
        dataSourceListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String selectedDataSource = getSelectedDataSource();
        		
        		// Sets the model name equal to datasource (if no entry in the modelname textbox)
        		setModelName(selectedDataSource);
        		
        		// Select the translator for the source
        		selectTranslatorForSource(selectedDataSource);
        		
            	updateDialogStatus();
        	}
        });
        
        modelNameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	updateDialogStatus();
            }
        });
        modelNameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
            	updateDialogStatus();
            }
        });
    }

    /**
     * Populate the DataSource ListBox
     */
    protected void doPopulateDataSourceListBox() {
        dataSourceService.getDataSources(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> sources) {
                populateDataSourceListBox(sources);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-datasources"), error); //$NON-NLS-1$
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
    private void populateDataSourceListBox(List<String> sources) {
    	// Make sure clear first
    	dataSourceListBox.clear();

    	dataSourceListBox.insertItem(NO_SELECTION, 0);
    	
    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String source: sources) {
    		dataSourceListBox.insertItem(source, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	dataSourceListBox.setSelectedIndex(0);
    }

    /**
     * Populate the Translator ListBox
     */
    protected void doPopulateTranslatorListBox() {
        dataSourceService.getTranslators(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> translators) {
                populateTranslatorListBox(translators);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-translators"), error); //$NON-NLS-1$
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
    private void populateTranslatorListBox(List<String> translators) {
    	// Make sure clear first
    	translatorListBox.clear();

    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String translator: translators) {
    		translatorListBox.insertItem(translator, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	translatorListBox.setSelectedIndex(0);
    }
    
    /**
     * Cache the Default Translator Mappings for later use.
     */
    protected void doPopulateDefaultTranslatorMappings() {
    	dataSourceService.getDefaultTranslatorMap(new IRpcServiceInvocationHandler<Map<String,String>>() {
    		@Override
    		public void onReturn(Map<String,String> defaultTranslatorsMap) {
    			defaultTranslatorMap = defaultTranslatorsMap;
    		}
    		@Override
    		public void onError(Throwable error) {
    			defaultTranslatorMap = new HashMap<String,String>();
    			notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-translatormappings"), error); //$NON-NLS-1$
    		}
    	});
    }
    
	private void selectTranslatorForSource(String selectedDataSource) {
		String defaultTranslator = getDefaultTranslatorForSource(selectedDataSource);
		
		int indx = 0;
		int nItems = translatorListBox.getItemCount();
		for(int i=0; i<nItems; i++) {
			String itemText = translatorListBox.getItemText(i);
			if(itemText.equalsIgnoreCase(defaultTranslator)) {
				indx = i;
				break;
			}
		}
		translatorListBox.setSelectedIndex(indx);
	}
    
    private String getDefaultTranslatorForSource(String dataSourceName) {
    	return defaultTranslatorMap.get(dataSourceName);
    }
    
    /**
     * Get the Model Name from the ModelName TextBox
     * @return the Model Name
     */
    public String getModelName( ) {
    	return modelNameTextBox.getText();
    }
    
    /**
     * Set the ModelName TextBox
     * @param modelName the Model Name
     */
    private void setModelName(String modelName) {
    	if(StringUtils.isEmpty(getModelName())) {
    		modelNameTextBox.setText(modelName);
    	}
    }
    
    /**
     * Get the selected DataSource Name from the DataSource ListBox
     * @return the DataSource Name
     */
    public String getSelectedDataSource( ) {
    	int selectedIndex = dataSourceListBox.getSelectedIndex();
    	String selectedDataSource = dataSourceListBox.getValue(selectedIndex);
    	return selectedDataSource;
    }
    
    /**
     * Get the selected Translator Name from the Translator ListBox
     * @return the Translator Name
     */
    public String getSelectedTranslator( ) {
    	int selectedIndex = translatorListBox.getSelectedIndex();
    	String selectedTranslator = translatorListBox.getValue(selectedIndex);
    	return selectedTranslator;
    }

    /**
     * @see org.jboss.datavirt.ui.client.local.widgets.bootstrap.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();
        
		statusLabel.setText(i18n.format("addSourceModelDialog.statusSelectDS"));
		
        doPopulateDataSourceListBox();
        doPopulateTranslatorListBox();
        doPopulateDefaultTranslatorMappings();

    }
    
    private void updateDialogStatus() {
    	boolean isValid = validateDialogSelections();
    	submitButton.setEnabled(isValid);
    }
    
    /*
     * Validate the dialog selections and return status. The status message label is also updated.
     * @return the dialog status. 'true' if selections are valid, 'false' otherwise.
     */
    private boolean validateDialogSelections( ) {
    	boolean isValid = true;
    	String statusStr = Constants.OK;

    	// Make sure a data source is selected
		String selectedDataSource = getSelectedDataSource();                                
    	if(NO_SELECTION.equals(selectedDataSource)) {
    		statusStr = i18n.format("addSourceModelDialog.statusSelectDS");
    		isValid = false;
    	}
    	
    	// Make sure Model Name is not empty
		String modelName = getModelName();
		if(isValid) {
			if(modelName==null || modelName.trim().length()==0) {
				statusStr = i18n.format("addSourceModelDialog.statusEnterModelName");
				isValid = false;
			}
		}
		
    	// Check entered name against existing names
    	if(isValid) {
    		if(currentModelNames.contains(modelName)) {
        		statusStr = i18n.format("addSourceModelDialog.statusModelNameAlreadyExists");
        		isValid = false;
    		}
    	}

    	// Update the status label
    	if(!statusStr.equals(Constants.OK)) {
    		statusLabel.setText(statusStr);
    	} else {
    		statusLabel.setText(i18n.format("addSourceModelDialog.statusClickOkToAccept"));
    	}

    	return isValid;
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("add-source-model-submit-button")
    protected void onSubmit(ClickEvent event) {
    	final String modelKey = "modelNameKey";
    	final String modelValue = getModelName();
    	final String dsKey = "dataSourceNameKey";
    	final String dsValue = getSelectedDataSource();
    	final String translatorKey = "translatorNameKey";
    	final String translatorValue = getSelectedTranslator();
    	Map<String,String> theMap = new HashMap<String,String>();
    	theMap.put(modelKey, modelValue);
    	theMap.put(dsKey, dsValue);
    	theMap.put(translatorKey, translatorValue);
    	
        ValueChangeEvent.fire(this, theMap);
        
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Map<String,String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

}
