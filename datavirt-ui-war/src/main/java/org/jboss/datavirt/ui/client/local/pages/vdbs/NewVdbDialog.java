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
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.services.VdbRpcService;
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
import com.google.gwt.user.client.ui.TextBox;

/**
 * Dialog that allows the user to create a new VDB
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/dialogs/new-vdb-dialog.html#new-vdb-dialog")
@Dependent
public class NewVdbDialog extends ModalDialog implements HasValueChangeHandlers<Map.Entry<String, String>> {

    @Inject
    protected ClientMessages i18n;
    @Inject @DataField("new-vdb-status-label")
    protected Label statusLabel;
    @Inject @DataField("new-vdb-name-textbox")
    protected TextBox vdbName;
    @Inject @DataField("new-vdb-submit-button")
    protected Button submitButton;

    @Inject
    protected VdbRpcService vdbService;

    private Collection<String> currentVdbNames;
    
    /**
     * Constructor.
     */
    public NewVdbDialog( ) {
    }
    
    /**
     * Constructor.
     */
    public NewVdbDialog(Collection<String> currentVdbNames) {
    	this.currentVdbNames = currentVdbNames;
    }
    
    public void setCurrentVdbNames(Collection<String> currentVdbNames) {
    	this.currentVdbNames = currentVdbNames;
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
        submitButton.setEnabled(false);
        
        vdbName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	updateDialogStatus();
            }
        });
    }

    /**
     * @see org.jboss.datavirt.ui.client.local.widgets.bootstrap.ModalDialog#show()
     */
    @Override
    public void show() {
        super.show();
        vdbName.setFocus(true);
		statusLabel.setText(i18n.format("newVdbDialog.statusEnterVdbName"));
    }

    private void updateDialogStatus() {
    	boolean isValid = validateVdbName();
    	submitButton.setEnabled(isValid);
    }
    
    /*
     * Validate the entered properties and return status. The status message label is also updated.
     * @return the property validation status. 'true' if properties are valid, 'false' otherwise.
     */
    private boolean validateVdbName( ) {
    	boolean isValid = true;
    	String statusStr = Constants.OK;

    	// Validate the entered name
    	String vdbNameStr = vdbName.getText();
    	if(isValid) {
    		if(vdbNameStr==null || vdbNameStr.trim().length()==0) {
    			statusStr = i18n.format("newVdbDialog.statusEnterVdbName");
    			isValid = false;
    		}
    	}
    	
    	// Check entered name against existing names
    	if(isValid) {
    		if(currentVdbNames.contains(vdbNameStr)) {
        		statusStr = i18n.format("newVdbDialog.statusVdbNameAlreadyExists");
        		isValid = false;
    		}
    	}

    	// Update the status label
    	if(!statusStr.equals(Constants.OK)) {
    		statusLabel.setText(statusStr);
    	} else {
    		statusLabel.setText(i18n.format("newVdbDialog.statusClickOkToAccept"));
    	}

    	return isValid;
    }
    
    public String getVdbName() {
    	return this.vdbName.getText();
    }
    
    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("new-vdb-submit-button")
    protected void onSubmit(ClickEvent event) {
        final String key = "vdbName";
        final String val = vdbName.getText();
        ValueChangeEvent.fire(this, new Map.Entry<String, String>() {
            @Override
            public String setValue(String value) {
                return null;
            }
            @Override
            public String getValue() {
                return val;
            }

            @Override
            public String getKey() {
                return key;
            }
        });
        hide();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Entry<String, String>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
    
}
