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
import org.jboss.datavirt.ui.client.local.pages.vdbs.VdbModelsTable;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.VdbRpcService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

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
    @Inject @DataField("back-to-dashboard")
    TransitionAnchor<DashboardPage> backToDashboard;
    @Inject @DataField("back-to-vdbs")
    TransitionAnchor<VirtualDatabasesPage> backToVdbs;

    @Inject @DataField("vdbdetails-pagetitle")
    protected Label pageTitle;
    @Inject @DataField("vdbdetails-vdbstatus")
    protected Label vdbStatusLabel;
    
    @Inject @DataField("model-search-box")
    protected TextBox searchBox;

    @Inject @DataField("btn-add-source")
    protected Button addSourceButton;
    @Inject @DataField("btn-add-view")
    protected Button addViewButton;
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
    
    @Inject @DataField("datavirt-vdbmodels-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("datavirt-vdbmodels-searching")
    protected HtmlSnippet searchInProgressMessage;
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
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
            	doGetVdbDetails();
            }
        });
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
        String searchText = (String) stateService.get(ApplicationStateKeys.VDBDETAILS_SEARCH_TEXT, ""); //$NON-NLS-1$
        Integer page = (Integer) stateService.get(ApplicationStateKeys.VDBDETAILS_PAGE, 1);
        SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.VDBDETAILS_SORT_COLUMN, vdbModelsTable.getDefaultSortColumn());

    	this.searchBox.setValue(searchText);
    	this.vdbModelsTable.sortBy(sortColumn.columnId, sortColumn.ascending);
    	
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
        //onSearchStarting();
        currentPage = page;
        currentVdbDetails = null;
        
		final String searchText = this.searchBox.getValue();
        final SortColumn currentSortColumn = this.vdbModelsTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.VDBDETAILS_SEARCH_TEXT, searchText);
        stateService.put(ApplicationStateKeys.VDBDETAILS_PAGE, currentPage);
        stateService.put(ApplicationStateKeys.VDBDETAILS_SORT_COLUMN, currentSortColumn);

        vdbService.getVdbDetails(vdbname, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
            	currentVdbDetails = vdbDetailsBean;
            	String title = "Virtual Database : "+vdbDetailsBean.getName();
            	pageTitle.setText(title);
            	vdbStatusLabel.setText(vdbDetailsBean.getStatus());
            	
                updateVdbModelsTable(vdbDetailsBean);
                updateDownloadVdbLink(vdbDetailsBean);
//                updatePager(data);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
//                searchInProgressMessage.setVisible(false);
                pageTitle.setText("unknown");
                vdbStatusLabel.setText("unknown");
            }
        });       
        
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
        String sourceVDBName = "VDBMgr-"+dsName+"-"+translator;
        vdbService.deploySourceVDBAddImportAndRedeploy(vdbname, sourceVDBName, dsName, translator, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
            	vdbStatusLabel.setText(vdbDetailsBean.getStatus());
            	
                updateVdbModelsTable(vdbDetailsBean);
//                updatePager(data);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
//                searchInProgressMessage.setVisible(false);
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
                	doAddViewModel(modelName,ddl);
                }
            }
        });
        dialog.show();
    }

    private void doAddViewModel(String modelName, String ddl) {
        vdbService.addOrReplaceViewModelAndRedeploy(vdbname, modelName, ddl, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
            	vdbStatusLabel.setText(vdbDetailsBean.getStatus());
            	
                updateVdbModelsTable(vdbDetailsBean);
//                updatePager(data);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
//                searchInProgressMessage.setVisible(false);
            }
        });           	
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
        vdbService.removeModelsAndRedeploy(vdbname, adjustedNameTypeMap, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vdbdetails.model-deleted"), //$NON-NLS-1$
                        i18n.format("vdbdetails.delete-success-msg")); //$NON-NLS-1$

            	vdbStatusLabel.setText(data.getStatus());
            	
                updateVdbModelsTable(data);
//                updatePager(data);
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
    	// Remove Model Button - enabled if at least one row is selected.
    	int selectedRows = this.vdbModelsTable.getSelectedModelNameAndTypeMap().size();
    	if(selectedRows==0) {
    		removeModelButton.setEnabled(false);
    	} else {
    		removeModelButton.setEnabled(true);
    	}
    }
    
    /**
     * Updates the table of VDB Models TAble with the given VdbDetailsBean.
     * @param data
     */
    protected void updateVdbModelsTable(VdbDetailsBean vdbDetails) {
        this.vdbModelsTable.clear();
        this.searchInProgressMessage.setVisible(false);
        if (vdbDetails.getModels().size() > 0) {
            for (VdbModelBean vdbModelBean : vdbDetails.getModels()) {
                this.vdbModelsTable.addRow(vdbModelBean);
            }
            this.vdbModelsTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }
    
    /**
     * Updates the pager with the given data.
     * @param data
     */
//    protected void updatePager(VdbDetailsBean data) {
//        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
//        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
//        this.pager.setNumPages(numPages);
//        this.pager.setPage(thisPage);
//        if (numPages > 1)
//            this.pager.setVisible(true);
//
//        int startIndex = data.getStartIndex() + 1;
//        int endIndex = startIndex + data.getDataSources().size() - 1;
//        String rangeText = "" + startIndex + "-" + endIndex; //$NON-NLS-1$ //$NON-NLS-2$
//        String totalText = String.valueOf(data.getTotalResults());
//        this.rangeSpan1.setInnerText(rangeText);
//        this.rangeSpan2.setInnerText(rangeText);
//        this.totalSpan1.setInnerText(totalText);
//        this.totalSpan2.setInnerText(totalText);
//    }

}
