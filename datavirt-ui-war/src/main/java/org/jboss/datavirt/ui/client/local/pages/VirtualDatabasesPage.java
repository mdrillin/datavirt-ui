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
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.pages.vdbs.DeleteVdbDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.IImportCompletionHandler;
import org.jboss.datavirt.ui.client.local.pages.vdbs.ImportVdbDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.NewVdbDialog;
import org.jboss.datavirt.ui.client.local.pages.vdbs.VdbsTable;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.VdbRpcService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbSummaryBean;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.widgets.bootstrap.Pager;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable.SortColumn;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The default "VirtualDatabases" page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/virtualdatabases.html#page")
@Page(path="virtualdatabases")
@Dependent
public class VirtualDatabasesPage extends AbstractPage {

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected VdbRpcService vdbService;
    @Inject
    protected NotificationService notificationService;
    @Inject
    protected ApplicationStateService stateService;
 
    // Breadcrumbs
    @Inject @DataField("back-to-dashboard")
    TransitionAnchor<DashboardPage> backToDashboard;

    @Inject @DataField("vdb-search-box")
    protected TextBox searchBox;

    @Inject @DataField("btn-new-vdb")
    protected Button newVdbButton;
    @Inject @DataField("btn-upload-vdb")
    protected Button uploadVdbButton;
    @Inject @DataField("btn-remove-vdb")
    protected Button removeVdbButton;
    @Inject @DataField("btn-test-vdb")
    protected Button testVdbButton;
    @Inject @DataField("btn-refresh")
    protected Button refreshButton;

    @Inject
    protected Instance<ImportVdbDialog> importVdbDialog;
    @Inject
    protected Instance<NewVdbDialog> newVdbDialog;
    @Inject
    DeleteVdbDialog deleteVdbDialog;
    
    @Inject @DataField("datavirt-virtualdatabases-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("datavirt-virtualdatabases-searching")
    protected HtmlSnippet searchInProgressMessage;
    @Inject @DataField("datavirt-virtualdatabases-table")
    protected VdbsTable vdbsTable;

    @Inject @DataField("datavirt-virtualdatabases-pager")
    protected Pager pager;
    @DataField("datavirt-virtualdatabases-range-1")
    protected SpanElement rangeSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-virtualdatabases-total-1")
    protected SpanElement totalSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-virtualdatabases-range-2")
    protected SpanElement rangeSpan2 = Document.get().createSpanElement();
    @DataField("datavirt-virtualdatabases-total-2")
    protected SpanElement totalSpan2 = Document.get().createSpanElement();

    private int currentPage = 1;
    private Collection<String> allVdbNames = new ArrayList<String>();
    
   /**
     * Constructor.
     */
    public VirtualDatabasesPage() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                doVdbSearch();
            }
        });
        pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
            	doVdbSearch(event.getValue());
            }
        });
        vdbsTable.addTableRowSelectionHandler(new TableRowSelectionEvent.Handler() {
            @Override
            public void onTableRowSelection(TableRowSelectionEvent event) {
                doSetButtonEnablements();
            }
        });
       vdbsTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
                doVdbSearch(currentPage);
            }
        });
       deleteVdbDialog.addClickHandler(new ClickHandler() {
           @Override
           public void onClick(ClickEvent event) {
               onDeleteVdbConfirm();
           }
       });
       
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }

    /**
     * Event handler that fires when the user clicks the New Vdb button.
     * @param event
     */
    @EventHandler("btn-new-vdb")
    public void onNewVdbClick(ClickEvent event) {
        NewVdbDialog dialog = newVdbDialog.get();
        dialog.setCurrentVdbNames(this.allVdbNames);
        
        // Value change is fired with vdbName and value on submit
        dialog.addValueChangeHandler(new ValueChangeHandler<Map.Entry<String,String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Entry<String, String>> event) {
                Entry<String, String> value = event.getValue();
                if (value != null) {
                    String vdbName = value.getValue();
                    doCreateDynamicVdb(vdbName);
                }
            }
        });

        dialog.show();
    }

    /**
     * Event handler that fires when the user clicks the Upload VDB button.
     * @param event
     */
    @EventHandler("btn-upload-vdb")
    public void onUploadVdbClick(ClickEvent event) {
        ImportVdbDialog dialog = importVdbDialog.get();
        dialog.setCompletionHandler(new IImportCompletionHandler() {
            @Override
            public void onImportComplete() {
                if (isAttached()) {
                    refreshButton.click();
                }
            }
        });
        dialog.show();
    }
    
    /**
     * Event handler that fires when the user clicks the Remove VDB button.
     * @param event
     */
    @EventHandler("btn-remove-vdb")
    public void onRemoveVdbClick(ClickEvent event) {
    	Collection<String> vdbNames = vdbsTable.getSelectedVdbs();
        deleteVdbDialog.setVdbNames(vdbNames);
        deleteVdbDialog.show();
    }
        
    /**
     * Called when the user confirms the dataSource deletion.
     */
    private void onDeleteVdbConfirm() {
    	Collection<String> vdbNames = this.vdbsTable.getSelectedVdbs();
    	String vdbText = null;
    	if(vdbNames.size()==1) {
    		vdbText = "VDB "+vdbNames.iterator().next();
    	} else {
    		vdbText = "VDB(s)";
    	}
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("vdbs.deleting-vdb-title"), //$NON-NLS-1$
                i18n.format("vdbs.deleting-vdb-msg", vdbText)); //$NON-NLS-1$
        vdbService.delete(vdbNames, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vdbs.vdb-deleted"), //$NON-NLS-1$
                        i18n.format("vdbs.delete-success-msg")); //$NON-NLS-1$

                // Refresh Page
            	doVdbSearch(currentPage);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vdbs.delete-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /**
     * Event handler that fires when the user clicks the Test button.
     * @param event
     */
    @EventHandler("btn-test-vdb")
    public void onTestSourceClick(ClickEvent event) {
    }

    /**
     * Event handler that fires when the user clicks the refresh button.
     * @param event
     */
    @EventHandler("btn-refresh")
    public void onRefreshClick(ClickEvent event) {
    	doVdbSearch(currentPage);
    }

    /**
     * Invoked on page showing.
     *
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        String searchText = (String) stateService.get(ApplicationStateKeys.VDBS_SEARCH_TEXT, ""); //$NON-NLS-1$
        Integer page = (Integer) stateService.get(ApplicationStateKeys.VDBS_PAGE, 1);
        SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.VDBS_SORT_COLUMN, vdbsTable.getDefaultSortColumn());

    	this.searchBox.setValue(searchText);
    	this.vdbsTable.sortBy(sortColumn.columnId, sortColumn.ascending);
    	
        // Kick off a VDB retrieval
    	doVdbSearch(page);
    }

    private void doSetButtonEnablements() {
    	// Test Button Disabled for now
    	testVdbButton.setEnabled(false);
    	
    	newVdbButton.setEnabled(true);
    	uploadVdbButton.setEnabled(true);
    	
    	// Remove DataSource Button - enabled if at least one row is selected.
    	int selectedRows = this.vdbsTable.getSelectedVdbs().size();
    	if(selectedRows==0) {
    		removeVdbButton.setEnabled(false);
    	} else {
    		removeVdbButton.setEnabled(true);
    	}
    }
    
    /**
     * Search for VDBs based on the current filter settings and search text.
     */
    protected void doVdbSearch() {
    	doVdbSearch(1);
    }

    /**
     * Search for VDBs based on the current filter settings and search text.
     * @param page
     */
    protected void doVdbSearch(int page) {
        onSearchStarting();
        currentPage = page;
		final String searchText = this.searchBox.getValue();
        final SortColumn currentSortColumn = this.vdbsTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.VDBS_SEARCH_TEXT, searchText);
        stateService.put(ApplicationStateKeys.VDBS_PAGE, currentPage);
        stateService.put(ApplicationStateKeys.VDBS_SORT_COLUMN, currentSortColumn);
        
        vdbService.search(searchText, page, currentSortColumn.columnId, currentSortColumn.ascending,
		        new IRpcServiceInvocationHandler<VdbResultSetBean>() {
            @Override
            public void onReturn(VdbResultSetBean data) {
            	allVdbNames = data.getAllVdbNames();
                updateVdbsTable(data);
                updatePager(data);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vdbs.error-searching"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                searchInProgressMessage.setVisible(false);
            }
        });

    }
    
    protected void doCreateDynamicVdb(String deploymentName) {
 	   
    	vdbService.createAndDeployDynamicVdb(deploymentName, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                doVdbSearch(currentPage);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vdbs.error-searching"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                searchInProgressMessage.setVisible(false);
            }
        });    	
    }

    /**
     * Called when a new VDB search is kicked off.
     */
    protected void onSearchStarting() {
        this.pager.setVisible(false);
        this.searchInProgressMessage.setVisible(true);
        this.vdbsTable.setVisible(false);
        this.noDataMessage.setVisible(false);
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }

    /**
     * Updates the table of VDBs with the given data.
     * @param data
     */
    protected void updateVdbsTable(VdbResultSetBean data) {
        this.vdbsTable.clear();
        this.searchInProgressMessage.setVisible(false);
        if (data.getVdbs().size() > 0) {
            for (VdbSummaryBean vdbSummaryBean : data.getVdbs()) {
                this.vdbsTable.addRow(vdbSummaryBean);
            }
            this.vdbsTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }

    /**
     * Updates the pager with the given data.
     * @param data
     */
    protected void updatePager(VdbResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        this.pager.setNumPages(numPages);
        this.pager.setPage(thisPage);
        if (numPages > 1)
            this.pager.setVisible(true);

        int startIndex = data.getStartIndex() + 1;
        int endIndex = startIndex + data.getVdbs().size() - 1;
        String rangeText = "" + startIndex + "-" + endIndex; //$NON-NLS-1$ //$NON-NLS-2$
        String totalText = String.valueOf(data.getTotalResults());
        this.rangeSpan1.setInnerText(rangeText);
        this.rangeSpan2.setInnerText(rangeText);
        this.totalSpan1.setInnerText(totalText);
        this.totalSpan2.setInnerText(totalText);
    }

}
