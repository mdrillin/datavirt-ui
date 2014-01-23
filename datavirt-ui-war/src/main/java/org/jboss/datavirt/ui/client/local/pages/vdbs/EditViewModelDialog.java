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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Dialog that allows the user to edit the View Model DDL
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/dialogs/edit-view-model-dialog.html#edit-view-model-dialog")
@Dependent
public class EditViewModelDialog extends ModalDialog implements HasValueChangeHandlers<Map<String,String>> {

    @Inject
    protected ClientMessages i18n;
    @Inject @DataField("editviewmodel-status-label")
    protected Label statusLabel;
    
    @Inject @DataField
    protected Label name;
    @Inject @DataField
    protected TextArea ddl;
    @Inject @DataField("edit-view-model-submit-button")
    protected Button submitButton;

    /**
     * Constructor.
     */
    public EditViewModelDialog() {
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
        ddl.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	updateDialogStatus();
            }
        });
        ddl.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
            	updateDialogStatus();
            }
        });
    }
    
    /*
     * Set the Model Name text
     * @param the model name text
     */
    public void setModelName(String modelName) {
    	this.name.setText(modelName);
    }
    
    /*
     * Get the Model Name text
     * @return the model name text
     */
    public String getModelName( ) {
    	return name.getText();
    }

    /*
     * Set the Model DDL
     * @param the model DDL
     */
    public void setDDL(String ddl) {
    	this.ddl.setText(ddl);
    }
    
    /*
     * Get the DDL Text
     * @return the DDL text
     */
    public String getDDL( ) {
    	return ddl.getText();
    }

    /**
     * @see org.jboss.datavirt.ui.client.local.widgets.bootstrap.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();

		statusLabel.setText(i18n.format("editViewModelDialog.statusEnterModelDDL"));

        ddl.setFocus(true);
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

    	// Make sure Model DDL is not empty
    	String ddl = getDDL();
    	if(ddl==null || ddl.trim().length()==0) {
    		statusStr = i18n.format("editViewModelDialog.statusEnterModelDDL");
    		isValid = false;
    	}
    	
    	// Update the status label
    	if(!statusStr.equals(Constants.OK)) {
    		statusLabel.setText(statusStr);
    	} else {
    		statusLabel.setText(i18n.format("editViewModelDialog.statusClickOkToAccept"));
    	}

    	return isValid;
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("edit-view-model-submit-button")
    protected void onSubmit(ClickEvent event) {
    	final String modelNameKey = "modelNameKey";
    	final String modelNameValue = getModelName();
    	final String ddlKey = "ddlKey";
    	final String ddlValue = getDDL();
    	Map<String,String> theMap = new HashMap<String,String>();
    	theMap.put(modelNameKey, modelNameValue);
    	theMap.put(ddlKey, ddlValue);
    	
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
