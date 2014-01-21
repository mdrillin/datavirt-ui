/*
 * Copyright 2012 JBoss Inc
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
package org.jboss.datavirt.ui.client.local.pages;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.pages.datasources.DataSourcePropertiesTable;
import org.jboss.datavirt.ui.client.local.pages.details.ApplyDataSourcePropertiesDialog;
import org.jboss.datavirt.ui.client.local.pages.details.ResetDataSourcePropertiesDialog;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;

/**
 * The page shown to the user when they click on one of the Data Sources
 * displayed in the Data Source Table on the DataSources page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/datasource-details.html#page")
@Page(path="datasourcedetails")
@Dependent
public class DataSourceDetailsPage extends AbstractPage {

    @Inject @DataField("to-datasources-page")
    private TransitionAnchor<DataSourcesPage> toDataSourcesPage;
    @Inject @DataField("to-datasource-types-page")
    private TransitionAnchor<DataSourceTypesPage> toDataSourceTypesPage;
    @Inject @DataField("to-vdbs-page")
    private TransitionAnchor<VirtualDatabasesPage> toVDBsPage;

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected DataSourceRpcService dataSourceService;
    @Inject
    protected NotificationService notificationService;
    
    protected DataSourceDetailsBean currentDataSourceDetails;

    @PageState
    private String name;

    // Breadcrumbs
    @Inject @DataField("back-to-datasources")
    TransitionAnchor<DataSourcesPage> backToDataSources;

    @Inject @DataField("datasourcedetails-pagetitle")
    protected Label pageTitle;

    @Inject @DataField("datasource-core-properties-table")
    protected DataSourcePropertiesTable dataSourceCorePropertiesTable;
    @Inject @DataField("datasource-advanced-properties-table")
    protected DataSourcePropertiesTable dataSourceAdvancedPropertiesTable;

    // Actions
    @Inject @DataField("btn-apply")
    protected Button applyButton;
    
    @Inject @DataField("btn-reset")
    protected Button resetButton;

    @Inject
    ApplyDataSourcePropertiesDialog applyDataSourcePropertiesDialog;
    @Inject
    ResetDataSourcePropertiesDialog resetDataSourcePropertiesDialog;
    
    @Inject @DataField("datasource-details-loading-spinner")
    protected HtmlSnippet datasourceLoading;

    /**
     * Constructor.
     */
    public DataSourceDetailsPage() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
    	dataSourceCorePropertiesTable.addValueChangeHandler(new ValueChangeHandler<Void>() {
            @Override
            public void onValueChange(ValueChangeEvent<Void> event) {
            	setButtonStates();
            }
        });
    	dataSourceAdvancedPropertiesTable.addValueChangeHandler(new ValueChangeHandler<Void>() {
            @Override
            public void onValueChange(ValueChangeEvent<Void> event) {
            	setButtonStates();
            }
        });
    	applyDataSourcePropertiesDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onApplyDataSourcePropertiesConfirmed();
            }
        });
    	resetDataSourcePropertiesDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onResetDataSourcePropertiesConfirmed();
            }
        });
    }

    /**
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
    	
        // Get the Data Source Details
    	doGetDataSourceDetails( );
    }

    /**
     * Get the Data Source details for the current Data Source name.
     */
    protected void doGetDataSourceDetails( ) {
        currentDataSourceDetails = null;
        
        dataSourceService.getDataSourceDetails(name, new IRpcServiceInvocationHandler<DataSourceDetailsBean>() {
            @Override
            public void onReturn(DataSourceDetailsBean dsDetailsBean) {
            	currentDataSourceDetails = dsDetailsBean;
            	String title = "Data Source : "+dsDetailsBean.getName();
            	pageTitle.setText(title);
            	
            	populatePropertiesTable(dsDetailsBean);
//                updateVdbModelsTable(vdbDetailsBean);
//                updateDownloadVdbLink(vdbDetailsBean);
//                updatePager(data);
            	setButtonStates();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
//                searchInProgressMessage.setVisible(false);
                pageTitle.setText("unknown");
            }
        });       
        
    }
    
    private void setButtonStates() {
    	boolean enableApply = false;
    	boolean enableReset = false;
    	
    	boolean coreTableHasChanges = this.dataSourceCorePropertiesTable.anyPropertyHasChanged();
    	boolean advTableHasChanges = this.dataSourceAdvancedPropertiesTable.anyPropertyHasChanged();
    	
    	if(coreTableHasChanges || advTableHasChanges) {
    		enableApply = true;
    		enableReset = true;
    	}
    	this.applyButton.setEnabled(enableApply);
    	this.resetButton.setEnabled(enableReset);
    }
    
    private void populatePropertiesTable(DataSourceDetailsBean dsDetailsBean) {
    	List<DataSourcePropertyBean> propList = dsDetailsBean.getProperties();
    	
    	dataSourceCorePropertiesTable.clear();
    	dataSourceAdvancedPropertiesTable.clear();
    	
    	for(DataSourcePropertyBean defn : propList) {
    		if(defn.isCoreProperty()) {
    			dataSourceCorePropertiesTable.addRow(defn);
    		} else {
    			dataSourceAdvancedPropertiesTable.addRow(defn);
    		}
    	}
    	dataSourceCorePropertiesTable.setVisible(true);
    	dataSourceAdvancedPropertiesTable.setVisible(true);
    }
    
    private void onApplyDataSourcePropertiesConfirmed() {
    	
    	DataSourceDetailsBean resultBean = new DataSourceDetailsBean();
    	resultBean.setName(currentDataSourceDetails.getName());
    	resultBean.setType(currentDataSourceDetails.getType());
    	
    	List<DataSourcePropertyBean> props = new ArrayList<DataSourcePropertyBean>();
    	List<DataSourcePropertyBean> coreProps = dataSourceCorePropertiesTable.getBeansWithRequiredOrNonDefaultValue();
    	List<DataSourcePropertyBean> advancedProps = dataSourceAdvancedPropertiesTable.getBeansWithRequiredOrNonDefaultValue();
    	props.addAll(coreProps);
    	props.addAll(advancedProps);
    	
    	resultBean.setProperties(props);
            	
    	// Redeploys the Data Source
    	doCreateDataSource(resultBean);
    }
    
    private void onResetDataSourcePropertiesConfirmed() {
    	dataSourceCorePropertiesTable.resetToOriginalValues();
    	dataSourceAdvancedPropertiesTable.resetToOriginalValues();
    	setButtonStates();
    }
    
    /**
     * Called when the user clicks the Apply edits button.
     * @param event
     */
    @EventHandler("btn-apply")
    protected void onApplyChanges(ClickEvent event) {
    	String dsName = currentDataSourceDetails.getName();
    	applyDataSourcePropertiesDialog.setDataSourceName(dsName);
    	applyDataSourcePropertiesDialog.show();
    }
    
    /**
     * Creates a DataSource
     * @param dsDetailsBean the data source details
     */
    private void doCreateDataSource(DataSourceDetailsBean detailsBean) {
    	final String dsName = detailsBean.getName();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("datasources.creating-datasource-title"), //$NON-NLS-1$
                i18n.format("datasources.creating-datasource-msg", dsName)); //$NON-NLS-1$
        dataSourceService.createDataSource(detailsBean, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("datasources.datasource-created"), //$NON-NLS-1$
                        i18n.format("datasources.create-success-msg")); //$NON-NLS-1$

                // Refresh Page
            	doGetDataSourceDetails();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("datasources.create-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /**
     * Called when the user clicks the Reset edits button.
     * @param event
     */
    @EventHandler("btn-reset")
    protected void onResetChanges(ClickEvent event) {
    	String dsName = currentDataSourceDetails.getName();
    	resetDataSourcePropertiesDialog.setDataSourceName(dsName);
    	resetDataSourcePropertiesDialog.show();
    }

    /**
     * Called when the user confirms the deletion.
     */
    protected void onDeleteConfirm() {
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("artifact-details.deleting-artifact-title"), //$NON-NLS-1$
//                i18n.format("artifact-details.deleting-artifact-msg", artifact.getModel().getName())); //$NON-NLS-1$
//        artifactService.delete(artifact.getModel(), new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("artifact-details.artifact-deleted"), //$NON-NLS-1$
//                        i18n.format("artifact-details.delete-success-msg", artifact.getModel().getName())); //$NON-NLS-1$
//                backToDataSources.click();
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("artifact-details.delete-error"), //$NON-NLS-1$
//                        error);
//            }
//        });
    }

}
