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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.ModalDialog;
import org.overlord.sramp.ui.client.local.widgets.common.TemplatedFormPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;

/**
 * A modal dialog used to import Data Source Types (drivers) into the Data Virtualization instance.
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/dialogs/import-datasource-type-dialog.html#import-datasource-type-dialog")
@Dependent
public class ImportDataSourceTypeDialog extends ModalDialog {

    @Inject @DataField("import-datasource-type-dialog-form")
    private TemplatedFormPanel form;
    @Inject @DataField("import-datasource-type-dialog-submit-button")
    private Button submitButton;

    @Inject
    private Instance<ImportDataSourceTypeFormSubmitHandler> formHandlerFactory;

    private ImportDataSourceTypeFormSubmitHandler formHandler;
    private IImportCompletionHandler completionHandler;

    /**
     * Constructor.
     */
    public ImportDataSourceTypeDialog() {
    }

    /**
     * Post construct.
     */
    @PostConstruct
    protected void onPostConstruct() {
        formHandler = formHandlerFactory.get();
        formHandler.setDialog(this);
        form.addSubmitHandler(formHandler);
        form.addSubmitCompleteHandler(formHandler);
    }

    /**
     * @see org.jboss.datavirt.ui.client.local.widgets.bootstrap.ModalDialog#show()
     */
    @Override
    public void show() {
        form.setAction(GWT.getModuleBaseURL() + "services/dataVirtUpload"); //$NON-NLS-1$
        super.show();
    }

    /**
     * Called when the user clicks the 'submit' (Import) button.
     * @param event
     */
    @EventHandler("import-datasource-type-dialog-submit-button")
    public void onSubmitClick(ClickEvent event) {
        formHandler.setCompletionHandler(this.completionHandler);
        form.submit();
    }

    /**
     * @return the completionHandler
     */
    public IImportCompletionHandler getCompletionHandler() {
        return completionHandler;
    }

    /**
     * @param completionHandler the completionHandler to set
     */
    public void setCompletionHandler(IImportCompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
    }

}
