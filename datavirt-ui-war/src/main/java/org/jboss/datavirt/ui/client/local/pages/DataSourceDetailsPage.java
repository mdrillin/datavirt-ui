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
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.pages.datasources.DataSourcePropertiesTable;
import org.jboss.datavirt.ui.client.local.pages.details.ApplyDataSourcePropertiesDialog;
import org.jboss.datavirt.ui.client.local.pages.details.ResetDataSourcePropertiesDialog;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
import org.jboss.datavirt.ui.client.shared.beans.PropertyBeanComparator;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable.SortColumn;

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
    @Inject @DataField("to-querytest-page")
    private TransitionAnchor<QueryTestPage> toQueryTestPage;

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected DataSourceRpcService dataSourceService;
    @Inject
    protected NotificationService notificationService;
    @Inject
    protected ApplicationStateService stateService;
    
    protected DataSourceDetailsBean currentDataSourceDetails;

    @PageState
    private String name;

    // Breadcrumbs
    @Inject @DataField("back-to-datasources")
    TransitionAnchor<DataSourcesPage> backToDataSources;

    @Inject @DataField("datasourcedetails-pagetitle")
    protected Label pageTitle;
    @Inject @DataField("datasourcedetails-jndiname")
    protected Label jndiLabel;

    @Inject @DataField("datavirt-datasourcedetails-status")
    protected Label statusMessage;
    
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
            	validatePage();
            }
        });
    	dataSourceAdvancedPropertiesTable.addValueChangeHandler(new ValueChangeHandler<Void>() {
            @Override
            public void onValueChange(ValueChangeEvent<Void> event) {
            	validatePage();
            }
        });
    	dataSourceCorePropertiesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
            	populateCorePropertiesTable(currentDataSourceDetails);
            }
        });
    	dataSourceAdvancedPropertiesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
            	populateAdvancedPropertiesTable(currentDataSourceDetails);
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
    	this.statusMessage.setVisible(true);
    	
        SortColumn sortColumnCore = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCE_DETAILS_SORT_COLUMN_CORE, dataSourceCorePropertiesTable.getDefaultSortColumn());
        SortColumn sortColumnAdv = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCE_DETAILS_SORT_COLUMN_ADV, dataSourceAdvancedPropertiesTable.getDefaultSortColumn());

    	this.dataSourceCorePropertiesTable.sortBy(sortColumnCore.columnId, sortColumnCore.ascending);
    	this.dataSourceAdvancedPropertiesTable.sortBy(sortColumnAdv.columnId, sortColumnAdv.ascending);

    	// Get the Data Source Details
    	doGetDataSourceDetails( );
    }

    /**
     * Get the Data Source details for the current Data Source name.
     */
    protected void doGetDataSourceDetails( ) {
        currentDataSourceDetails = null;
        
        final SortColumn currentSortColumnCore = this.dataSourceCorePropertiesTable.getCurrentSortColumn();
        final SortColumn currentSortColumnAdv = this.dataSourceAdvancedPropertiesTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.DATASOURCE_DETAILS_SORT_COLUMN_CORE, currentSortColumnCore);
        stateService.put(ApplicationStateKeys.DATASOURCE_DETAILS_SORT_COLUMN_ADV, currentSortColumnAdv);

        dataSourceService.getDataSourceDetails(name, new IRpcServiceInvocationHandler<DataSourceDetailsBean>() {
            @Override
            public void onReturn(DataSourceDetailsBean dsDetailsBean) {
            	currentDataSourceDetails = dsDetailsBean;
            	String title = "Data Source : "+dsDetailsBean.getName();
            	pageTitle.setText(title);
            	String jndiStr = "JNDI : "+dsDetailsBean.getJndiName();
            	jndiLabel.setText(jndiStr);   
            	
            	populateCorePropertiesTable(dsDetailsBean);
            	populateAdvancedPropertiesTable(dsDetailsBean);
            	validatePage();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("datasourcedetails.error-retrieving"), error); //$NON-NLS-1$
                pageTitle.setText("Data Source : Error retrieving");
            }
        });       
        
    }
    
    /*
     * Validates the page - set the status message text and button states
     */
    private void validatePage() {
    	// ------------------
    	// Set status message
    	// ------------------
    	String overallStatus = this.dataSourceCorePropertiesTable.getStatus();
    	if(overallStatus.equalsIgnoreCase(Constants.OK)) {
    		overallStatus = this.dataSourceAdvancedPropertiesTable.getStatus();
    	}

    	boolean coreTableHasChanges = this.dataSourceCorePropertiesTable.anyPropertyHasChanged();
    	boolean advTableHasChanges = this.dataSourceAdvancedPropertiesTable.anyPropertyHasChanged();
    	boolean propertiesChangedAndValid = false;
    	
    	if(overallStatus.equalsIgnoreCase(Constants.OK)) {
    		// Determine if any property changes
    		if(!coreTableHasChanges && !advTableHasChanges) {
        		statusMessage.setText(i18n.format("datasourcedetails.statusMessage-nochanges"));
        	// Has changes and valid
    		} else {
    			propertiesChangedAndValid = true;
    			statusMessage.setText(i18n.format("datasourcedetails.statusMessage-ok"));
    		}
    	} else {
    		statusMessage.setText(overallStatus);
    	}
    	
    	// ------------------
    	// Set button states
    	// ------------------
    	if(coreTableHasChanges || advTableHasChanges) {
        	this.resetButton.setEnabled(true);
    	} else {
    		this.resetButton.setEnabled(false);
    	}
    	
    	if(propertiesChangedAndValid) {
    		this.applyButton.setEnabled(true);
    	} else {
    		this.applyButton.setEnabled(false);
    	}
    }
    
    /*
     * Populate the core properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateCorePropertiesTable(DataSourceDetailsBean dsDetailsBean) {
    	List<DataSourcePropertyBean> propList = dsDetailsBean.getProperties();
    	dataSourceCorePropertiesTable.clear();

        final SortColumn currentSortColumnCore = this.dataSourceCorePropertiesTable.getCurrentSortColumn();
        stateService.put(ApplicationStateKeys.DATASOURCE_DETAILS_SORT_COLUMN_CORE, currentSortColumnCore);

    	// Separate property types, sorted in correct order
    	List<DataSourcePropertyBean> corePropList = getPropList(propList, true, !currentSortColumnCore.ascending);
    	// Populate core properties table
    	for(DataSourcePropertyBean defn : corePropList) {
   			dataSourceCorePropertiesTable.addRow(defn);
    	}
    	dataSourceCorePropertiesTable.setVisible(true);
    }

    /*
     * Populate the advanced properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateAdvancedPropertiesTable(DataSourceDetailsBean dsDetailsBean) {
    	List<DataSourcePropertyBean> propList = dsDetailsBean.getProperties();
    	dataSourceAdvancedPropertiesTable.clear();

        final SortColumn currentSortColumnAdv = this.dataSourceAdvancedPropertiesTable.getCurrentSortColumn();
        stateService.put(ApplicationStateKeys.DATASOURCE_DETAILS_SORT_COLUMN_ADV, currentSortColumnAdv);

    	// Separate property types, sorted in correct order
    	List<DataSourcePropertyBean> advPropList = getPropList(propList, false, !currentSortColumnAdv.ascending);
    	// Populate advanced properties table
    	for(DataSourcePropertyBean defn : advPropList) {
    		dataSourceAdvancedPropertiesTable.addRow(defn);
    	}
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
    
    /*
     * Re-deploys the Data Source using the properties from the Tables
     */
    private void onApplyDataSourcePropertiesConfirmed() {
    	
    	DataSourceDetailsBean resultBean = new DataSourceDetailsBean();
    	resultBean.setName(currentDataSourceDetails.getName());
    	resultBean.setJndiName(currentDataSourceDetails.getJndiName());
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
    
    /*
     * Reset the table values to the original values
     */
    private void onResetDataSourcePropertiesConfirmed() {
    	dataSourceCorePropertiesTable.resetToOriginalValues();
    	dataSourceAdvancedPropertiesTable.resetToOriginalValues();
    	validatePage();
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

}
