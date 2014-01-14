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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Dialog that allows the user to delete a Model from a VDB
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/dialogs/delete-vdbmodel-dialog.html#delete-vdbmodel-dialog")
@Dependent
public class DeleteVdbModelDialog extends ModalDialog implements HasClickHandlers {

    @Inject @DataField("vdbmodel-name")
    protected InlineLabel modelName;
    @Inject @DataField("delete-vdbmodel-submit-button")
    protected Button submitButton;

    /**
     * Constructor.
     */
    public DeleteVdbModelDialog() {
    }

    /**
     * Called when the dialog is constructed by Errai.
     */
    @PostConstruct
    protected void onPostConstruct() {
    }

    /**
     * Sets the Collection of VDB model names requested to be deleted.
     * @param modelNames the collection of model names.
     */
    public void setModelNames(Collection<String> modelNames) {
    	String nameLabel = null;
    	if(modelNames.size()==1) {
    		nameLabel = "Model " + modelNames.iterator().next();
    	} else {
    		nameLabel = "the selected Models";
    	}
        this.modelName.setText(nameLabel);
    }

    /**
     * Called when the user clicks the submit button.
     * @param event
     */
    @EventHandler("delete-vdbmodel-submit-button")
    protected void onSubmit(ClickEvent event) {
        hide();
    }

    /**
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return submitButton.addClickHandler(handler);
    }
}
