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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.pages.vdbs.AddSourceModelDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.AddViewModelDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.DeleteVdbModelDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.EditViewModelDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.VdbModelsTable;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.VdbRpcService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbModelBean;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.Pager;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable.SortColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * The page shown to the user when they click on one of the VDBs
 * displayed in the VDB Table on the VDBs page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/vdb-details.html#page")
@Page(path="vdbdetails")
@Dependent
public class VdbDetailsPage extends AbstractPage {

    @Inject @DataField("to-datasources-page")
    private TransitionAnchor<DataSourcesPage> toDataSourcesPage;
    @Inject @DataField("to-datasource-types-page")
    private TransitionAnchor<DataSourceTypesPage> toDataSourceTypesPage;
    @Inject @DataField("to-vdbs-page")
    private TransitionAnchor<VirtualDatabasesPage> toVDBsPage;

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected VdbRpcService vdbService;
    @Inject
    protected NotificationService notificationService;
    @Inject
    protected ApplicationStateService stateService;

    @PageState
    private String vdbname;

    // Breadcrumbs
    @Inject @DataField("back-to-vdbs")
    TransitionAnchor<VirtualDatabasesPage> backToVdbs;

    @Inject @DataField("vdbdetails-pagetitle")
    protected Label pageTitle;
    @Inject @DataField("vdbdetails-vdbstatus")
    protected Label vdbStatusLabel;
    @Inject @DataField("vdbdetails-vdbstatus-image")
    protected Image vdbStatusImage;
    
    @Inject @DataField("btn-add-source")
    protected Button addSourceButton;
    @Inject @DataField("btn-add-view")
    protected Button addViewButton;
    @Inject @DataField("btn-edit")
    protected Button editModelButton;
    @Inject @DataField("btn-remove")
    protected Button removeModelButton;
    @Inject @DataField("btn-refresh")
    protected Button refreshButton;
    @Inject @DataField("link-download-dynamicvdb")
    protected Anchor downloadDynamicVdbLink;

    @Inject
    DeleteVdbModelDialog deleteVdbModelDialog;
    @Inject
    protected Instance<AddSourceModelDialog> addSourceModelDialogFactory;
    @Inject
    protected Instance<AddViewModelDialog> addViewModelDialogFactory;
    @Inject
    protected Instance<EditViewModelDialog> editViewModelDialogFactory;
    
    @Inject @DataField("datavirt-vdbmodels-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("datavirt-vdbmodels-retrieving")
    protected HtmlSnippet getModelsInProgressMessage;
    @Inject @DataField("datavirt-vdbmodels-adding")
    protected HtmlSnippet addModelInProgressMessage;
    @Inject @DataField("datavirt-vdbmodels-table")
    protected VdbModelsTable vdbModelsTable;

    @Inject @DataField("datavirt-vdbmodels-pager")
    protected Pager pager;
    @DataField("datavirt-vdbmodels-range-1")
    protected SpanElement rangeSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-vdbmodels-total-1")
    protected SpanElement totalSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-vdbmodels-range-2")
    protected SpanElement rangeSpan2 = Document.get().createSpanElement();
    @DataField("datavirt-vdbmodels-total-2")
    protected SpanElement totalSpan2 = Document.get().createSpanElement();
    
    @Inject @DataField("vdbdetails-loading-spinner")
    protected HtmlSnippet datasourceLoading;

    private int currentPage = 1;
    protected VdbDetailsBean currentVdbDetails;
        
	/**
     * Constructor.
     */
    public VdbDetailsPage() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
        pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
            	doGetVdbDetails(event.getValue());
            }
        });
        vdbModelsTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
            	doGetVdbDetails(currentPage);
            }
        });
        vdbModelsTable.addTableRowSelectionHandler(new TableRowSelectionEvent.Handler() {
            @Override
            public void onTableRowSelection(TableRowSelectionEvent event) {
                doSetButtonEnablements();
            }
        });       
        deleteVdbModelDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteModelConfirm();
            }
        });
        
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$

    }

    /**
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        Integer page = (Integer) stateService.get(ApplicationStateKeys.VDBDETAILS_PAGE, 1);
        SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.VDBDETAILS_SORT_COLUMN, vdbModelsTable.getDefaultSortColumn());

    	this.vdbModelsTable.sortBy(sortColumn.columnId, !sortColumn.ascending);
    	
        // Kick off an vdb details retrieval
    	doGetVdbDetails(page);
    }

    /**
     * Get the VDB Details
     */
    protected void doGetVdbDetails() {
        Integer page = (Integer) stateService.get(ApplicationStateKeys.VDBDETAILS_PAGE, 1);
        doGetVdbDetails(page);
    }
    
    /**
     * Get the VDB Details
     * @param page
     */
    protected void doGetVdbDetails(int page) {
        onGetModelsStarting();
        currentPage = page;
        currentVdbDetails = null;
        
        final SortColumn currentSortColumn = this.vdbModelsTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.VDBDETAILS_PAGE, currentPage);
        stateService.put(ApplicationStateKeys.VDBDETAILS_SORT_COLUMN, currentSortColumn);

        vdbService.getVdbDetails(vdbname, currentPage, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
            	currentVdbDetails = vdbDetailsBean;
            	String title = "Virtual Database : "+vdbDetailsBean.getName();
            	pageTitle.setText(title);
            	
            	setVdbStatus(vdbDetailsBean);
            	
                updateVdbModelsTable(vdbDetailsBean);
                updateDownloadVdbLink(vdbDetailsBean);
                updatePager(vdbDetailsBean);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vdbdetails.error-retrieving-details"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
            	getModelsInProgressMessage.setVisible(false);
                pageTitle.setText("unknown");
                vdbStatusLabel.setText("unknown");
            }
        });       
        
    }
    
    /**
     * Sets the VDB Status title and image
     * @param vdbDetails
     */
    private void setVdbStatus(VdbDetailsBean vdbDetails) {
    	String statusText = vdbDetails.getStatus();
    	vdbStatusLabel.setText(statusText);
    	if(statusText.equalsIgnoreCase("active")) {
    		vdbStatusImage.setTitle("The VDB is active");
    		vdbStatusImage.setUrl(Constants.VDB_STATUS_URL_ACTIVE_32PX);
    	} else if(statusText.equalsIgnoreCase("inactive")) {
    		vdbStatusImage.setTitle("The VDB is inactive");
    		vdbStatusImage.setUrl(Constants.VDB_STATUS_URL_INACTIVE_32PX);
    	} else if(statusText.equalsIgnoreCase("loading")) {
    		vdbStatusImage.setTitle("The VDB is loading");
    		vdbStatusImage.setUrl(Constants.VDB_STATUS_URL_LOADING_32PX);
    	}
    }
    
    private void updateDownloadVdbLink(VdbDetailsBean vdbDetailsBean) {
    	String contentUrl = GWT.getModuleBaseURL() + "services/dataVirtDownload?vdbname="+vdbDetailsBean.getName(); //$NON-NLS-1$
    	downloadDynamicVdbLink.setHref(contentUrl);
    	downloadDynamicVdbLink.setVisible(true);
    }
    
    /**
     * Called when the user clicks the Add Source Model button.
     * @param event
     */
    @EventHandler("btn-add-source")
    protected void onAddSourceClick(ClickEvent event) {
        AddSourceModelDialog dialog = addSourceModelDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<Map<String,String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Map<String, String>> event) {
                Map<String, String> value = event.getValue();
                if (value != null) {
                	String dsName = value.get("dataSourceNameKey");
                	String translator = value.get("translatorNameKey");
                	doAddSourceModel(dsName,translator);
                }
            }
        });
        dialog.show();
    }
    
    private void doAddSourceModel(String dsName, String translator) {
        onAddModelsStarting();
        
        String sourceVDBName = "VDBMgr-"+dsName+"-"+translator;
        vdbService.deploySourceVDBAddImportAndRedeploy(vdbname, currentPage, sourceVDBName, dsName, translator, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
            	currentVdbDetails = vdbDetailsBean;
            	setVdbStatus(vdbDetailsBean);
                updateVdbModelsTable(vdbDetailsBean);
                updatePager(vdbDetailsBean);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vdbdetails.error-adding-source-model"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
    
    /**
     * Called when the user clicks the Add View Model button.
     * @param event
     */
    @EventHandler("btn-add-view")
    protected void onAddViewClick(ClickEvent event) {
    	AddViewModelDialog dialog = addViewModelDialogFactory.get();
        dialog.addValueChangeHandler(new ValueChangeHandler<Map<String,String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Map<String, String>> event) {
                Map<String, String> value = event.getValue();
                if (value != null) {
                	String modelName = value.get("modelNameKey");
                	String ddl = value.get("ddlKey");
                	doAddOrReplaceViewModel(modelName,ddl);
                }
            }
        });
        dialog.show();
    }

    private void doAddOrReplaceViewModel(String modelName, String ddl) {
        onAddModelsStarting();

        vdbService.addOrReplaceViewModelAndRedeploy(vdbname, currentPage, modelName, ddl, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
            	currentVdbDetails = vdbDetailsBean;
            	setVdbStatus(vdbDetailsBean);
                updateVdbModelsTable(vdbDetailsBean);
                updatePager(vdbDetailsBean);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vdbdetails.error-adding-view-model"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
    
    /**
     * Event handler that fires when the user clicks the Edit model button.  Can only edit the View Model - to change the DDL
     * @param event
     */
    @EventHandler("btn-edit")
    public void onEditSourceClick(ClickEvent event) {
    	// Edit will only have one selection
    	String modelName = vdbModelsTable.getSelectedModelNameAndTypeMap().keySet().iterator().next();
    	String modelType = vdbModelsTable.getSelectedModelNameAndTypeMap().get(modelName);
    	
       	// View Model
    	if(modelType.equalsIgnoreCase("VIRTUAL")) {
    		EditViewModelDialog dialog = editViewModelDialogFactory.get();
    		dialog.setModelName(modelName);
        	String ddl = getModelDdl(modelName,currentVdbDetails);
    		dialog.setDDL(ddl);
    		dialog.addValueChangeHandler(new ValueChangeHandler<Map<String,String>>() {
    			@Override
    			public void onValueChange(ValueChangeEvent<Map<String, String>> event) {
    				Map<String, String> value = event.getValue();
    				if (value != null) {
    					String dialogModelName = value.get("modelNameKey");
    					String dialogDdl = value.get("ddlKey");
    					doAddOrReplaceViewModel(dialogModelName,dialogDdl);
    				}
    			}
    		});
    		dialog.show();

    	}
        
    }
    
    /*
     * Get the Model DDL for the specified model name from the VdbDetailsBean
     * @param modelName the name of the model
     * @param vdbDetails the VdbDetailsBean
     * @return the model DDL
     */
    private String getModelDdl(String modelName, VdbDetailsBean vdbDetails) {
    	String ddl = null;
    	
    	VdbModelBean modelBean = vdbDetails.getModel(modelName);
    	if(modelBean != null) {
    		ddl = modelBean.getDdl();
    	}
    	
    	return ddl;
    }
    
    /**
     * Event handler that fires when the user clicks the RemoveModel button.
     * @param event
     */
    @EventHandler("btn-remove")
    public void onRemoveSourceClick(ClickEvent event) {
    	Collection<String> modelNames = vdbModelsTable.getSelectedModelNameAndTypeMap().keySet();
        deleteVdbModelDialog.setModelNames(modelNames);
        deleteVdbModelDialog.show();
    }
        
    /**
     * Called when the user confirms the Model deletion.
     */
    private void onDeleteModelConfirm() {
    	Map<String,String> modelNameAndTypeMap = this.vdbModelsTable.getSelectedModelNameAndTypeMap();
    	Map<String,String> modelNameAndTranslatorMap = this.vdbModelsTable.getSelectedModelNameAndTranslatorMap();
    	String modelText = null;
    	if(modelNameAndTypeMap.size()==1) {
    		modelText = "Model "+modelNameAndTypeMap.keySet().iterator().next();
    	} else {
    		modelText = "Model(s)";
    	}
    	final String modelTextFinal = modelText;
    	
    	// Adjust the Map Entries for the View 'Import' names
    	Map<String,String> adjustedNameTypeMap = new HashMap<String,String>(modelNameAndTypeMap.size());
    	for(String modelName : modelNameAndTypeMap.keySet()) {
    		String type = modelNameAndTypeMap.get(modelName);
    		String translator = modelNameAndTranslatorMap.get(modelName);
    		if(type.equalsIgnoreCase("PHYSICAL")) {
    			adjustedNameTypeMap.put("VDBMgr-"+modelName+"-"+translator, type);
    		} else {
    			adjustedNameTypeMap.put(modelName, type);
    		}
    	}
    	
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("vdbdetails.deleting-model-title"), //$NON-NLS-1$
                i18n.format("vdbdetails.deleting-model-msg", modelText)); //$NON-NLS-1$
        vdbService.removeModelsAndRedeploy(vdbname, currentPage, adjustedNameTypeMap, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vdbdetails.model-deleted"), //$NON-NLS-1$
                        i18n.format("vdbdetails.delete-success-msg", modelTextFinal)); //$NON-NLS-1$

            	currentVdbDetails = vdbDetailsBean;
            	setVdbStatus(vdbDetailsBean);
            	
                updateVdbModelsTable(vdbDetailsBean);
                updatePager(vdbDetailsBean);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vdbdetails.delete-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /**
     * Event handler that fires when the user clicks the refresh button.
     * @param event
     */
    @EventHandler("btn-refresh")
    public void onRefreshClick(ClickEvent event) {
    	doGetVdbDetails(currentPage);
    }
    
    private void doSetButtonEnablements() {
    	String vdbType = currentVdbDetails.getType();
    	if(vdbType!=null && vdbType.equalsIgnoreCase("archive")) {
    	    addSourceButton.setVisible(false);
    	    addViewButton.setVisible(false);
    	    editModelButton.setVisible(false);
    	    removeModelButton.setVisible(false);
    	    downloadDynamicVdbLink.setVisible(false);
    	} else {
    	    addSourceButton.setVisible(true);
    	    addViewButton.setVisible(true);
    	    editModelButton.setVisible(true);
    	    removeModelButton.setVisible(true);
    	    downloadDynamicVdbLink.setVisible(true);

    	    // Get number of rows selected
    		int selectedRows = this.vdbModelsTable.getSelectedModelNameAndTypeMap().size();
    		
    	    // Edit Model Button enablement - enable for single virtual model selection
    	    if(selectedRows==1) {
    	    	String modelName = this.vdbModelsTable.getSelectedModelNameAndTypeMap().keySet().iterator().next();
    	    	String modelType = this.vdbModelsTable.getSelectedModelNameAndTypeMap().get(modelName);
    	    	if(modelType.equalsIgnoreCase("VIRTUAL")) {
    	    		editModelButton.setEnabled(true);
    	    	} else {
    	    		editModelButton.setEnabled(false);
    	    	}
    	    } else {
    			editModelButton.setEnabled(false);
    	    }
    	    
    	    // Remove Model Button - enabled if at least one row is selected.
    		if(selectedRows==0) {
    			removeModelButton.setEnabled(false);
    		} else {
    			removeModelButton.setEnabled(true);
    		}
    	}
    }
    
    /**
     * Updates the table of VDB Models TAble with the given VdbDetailsBean.
     * @param vdbDetails
     */
    protected void updateVdbModelsTable(VdbDetailsBean vdbDetails) {
    	if( vdbDetails.getType().equals("dynamic") ) {
    		this.vdbModelsTable.setEditable(true);
    	} else {
    		this.vdbModelsTable.setEditable(false);
    	}
        this.vdbModelsTable.clear();
        this.getModelsInProgressMessage.setVisible(false);
        this.addModelInProgressMessage.setVisible(false);
        if (vdbDetails.getModels().size() > 0) {
            for (VdbModelBean vdbModelBean : vdbDetails.getModels()) {
                this.vdbModelsTable.addRow(vdbModelBean);
            }
            this.noDataMessage.setVisible(false);
            this.vdbModelsTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
            this.vdbModelsTable.setVisible(false);
        }
    }
    
    /**
     * Called when the details retrieval is kicked off.
     */
    protected void onGetModelsStarting() {
        this.getModelsInProgressMessage.setVisible(true);
		this.vdbStatusImage.setTitle("The VDB is loading");
		this.vdbStatusImage.setUrl(Constants.VDB_STATUS_URL_LOADING_32PX);
		this.vdbStatusLabel.setText("LOADING");
        this.pager.setVisible(false);
        this.addModelInProgressMessage.setVisible(false);
        this.vdbModelsTable.setVisible(false);
        this.noDataMessage.setVisible(false);
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }

    /**
     * Called when model addition is started
     */
    protected void onAddModelsStarting() {
        this.addModelInProgressMessage.setVisible(true);
		this.vdbStatusImage.setTitle("The VDB is loading");
		this.vdbStatusImage.setUrl(Constants.VDB_STATUS_URL_LOADING_32PX);
		this.vdbStatusLabel.setText("LOADING");
        this.pager.setVisible(false);
        this.getModelsInProgressMessage.setVisible(false);
        this.vdbModelsTable.setVisible(false);
        this.noDataMessage.setVisible(false);
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }
    
    /**
     * Updates the pager with the given data.
     * @param data
     */
    protected void updatePager(VdbDetailsBean data) {
        int numPages = ((int) (data.getTotalModels() / data.getModelsPerPage())) + (data.getTotalModels() % data.getModelsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getModelsPerPage()) + 1;
        this.pager.setNumPages(numPages);
        this.pager.setPage(thisPage);
        if (numPages > 1)
            this.pager.setVisible(true);

        int startIndex = data.getStartIndex();
        int endBatchIndx = startIndex + data.getModelsPerPage();
        int endAllIndx = data.getTotalModels();
        int endIndex = (endBatchIndx <= endAllIndx) ? endBatchIndx : endAllIndx;
        
        // reset start index to zero if end index is zero
        startIndex = (endIndex==0) ? endIndex : startIndex+1;
        
        String rangeText = "" + startIndex + "-" + endIndex; //$NON-NLS-1$ //$NON-NLS-2$
        String totalText = String.valueOf(data.getTotalModels());
        this.rangeSpan1.setInnerText(rangeText);
        this.rangeSpan2.setInnerText(rangeText);
        this.totalSpan1.setInnerText(totalText);
        this.totalSpan2.setInnerText(totalText);
    }

}
