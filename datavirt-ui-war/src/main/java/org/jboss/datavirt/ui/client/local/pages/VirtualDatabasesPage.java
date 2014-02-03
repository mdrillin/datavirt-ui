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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.pages.datasources.DVPager;
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
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.VdbSummaryBean;
import org.jboss.datavirt.ui.client.shared.services.StringUtils;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.events.TableSortEvent;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.local.widgets.common.SortableTemplatedWidgetTable.SortColumn;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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

    @Inject @DataField("to-datasources-page")
    private TransitionAnchor<DataSourcesPage> toDataSourcesPage;
    @Inject @DataField("to-vdbs-page")
    private TransitionAnchor<VirtualDatabasesPage> toVDBsPage;
    @Inject @DataField("to-querytest-page")
    private TransitionAnchor<QueryTestPage> toQueryTestPage;
    @Inject
    protected TransitionAnchorFactory<VdbDetailsPage> toDetailsPageLinkFactory;

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected VdbRpcService vdbService;
    @Inject
    protected NotificationService notificationService;
    @Inject
    protected ApplicationStateService stateService;
 
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
    protected DVPager pager;

    private int currentPage = 1;
    private Collection<String> allVdbNames = new ArrayList<String>();
    private Map<String, Boolean> vdbTestableMap = new HashMap<String,Boolean>();
    
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
        searchBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	searchIfValueChanged();
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

                // Deletion - go back to page 1 - delete could have made current page invalid
            	doVdbSearch(1);
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
    public void onTestVdbClick(ClickEvent event) {
    	// Get the selected source - set the application state.  Then go to Test page.
    	String selectedSource = vdbsTable.getSelectedVdbs().iterator().next();
		stateService.put(ApplicationStateKeys.QUERY_SOURCELIST_SELECTED, selectedSource);
		
		toQueryTestPage.click();
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
        String filterText = (String) stateService.get(ApplicationStateKeys.VDBS_FILTER_TEXT, ""); //$NON-NLS-1$
        Integer page = (Integer) stateService.get(ApplicationStateKeys.VDBS_PAGE, 1);
        SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.VDBS_SORT_COLUMN, vdbsTable.getDefaultSortColumn());

    	this.searchBox.setValue(filterText);
    	this.vdbsTable.sortBy(sortColumn.columnId, sortColumn.ascending);
    	
        // Kick off a VDB retrieval
    	doVdbSearch(page);
    }

    private void doSetButtonEnablements() {
    	newVdbButton.setEnabled(true);
    	uploadVdbButton.setEnabled(true);
    	
    	// Remove DataSource Button - enabled if at least one row is selected.
    	int selectedRows = this.vdbsTable.getSelectedVdbs().size();
    	if(selectedRows==0) {
    		removeVdbButton.setEnabled(false);
    	} else {
    		removeVdbButton.setEnabled(true);
    	}
    	
    	// Test Vdb Button - enabled if only one row is selected - and vdb is testable
    	if(selectedRows==1) {
    		String selectedSource = this.vdbsTable.getSelectedVdbs().iterator().next();
    		boolean isTestable = this.vdbTestableMap.get(selectedSource);
    		testVdbButton.setEnabled(isTestable);
    	} else {
    		testVdbButton.setEnabled(false);
    	}
    }
    
    private void searchIfValueChanged() {
    	// Current Search Text state
        String appSearchText = (String) stateService.get(ApplicationStateKeys.VDBS_FILTER_TEXT, ""); //$NON-NLS-1$
        // SearchBox text
        String searchBoxText = this.searchBox.getText();
        // Search if different
        if(!StringUtils.equals(appSearchText, searchBoxText)) {
        	doVdbSearch();
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
		final String filterText = this.searchBox.getValue();
        final SortColumn currentSortColumn = this.vdbsTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.VDBS_FILTER_TEXT, filterText);
        stateService.put(ApplicationStateKeys.VDBS_PAGE, currentPage);
        stateService.put(ApplicationStateKeys.VDBS_SORT_COLUMN, currentSortColumn);
        
        vdbService.search(filterText, page, false, currentSortColumn.columnId, !currentSortColumn.ascending,
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
    
    protected void doCreateDynamicVdb(final String deploymentName) {
 	   
    	vdbService.createAndDeployDynamicVdb(deploymentName, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                TransitionAnchor<VdbDetailsPage> detailsLink = toDetailsPageLinkFactory.get("vdbname", deploymentName); //$NON-NLS-1$
                detailsLink.click();
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
    }

    /**
     * Updates the table of VDBs with the given data.  Also updated Vdb testable map
     * @param data
     */
    protected void updateVdbsTable(VdbResultSetBean data) {
        this.vdbsTable.clear();
        this.vdbTestableMap.clear();
        this.searchInProgressMessage.setVisible(false);
        if (data.getVdbs().size() > 0) {
            for (VdbSummaryBean vdbSummaryBean : data.getVdbs()) {
                this.vdbsTable.addRow(vdbSummaryBean);
                if(vdbSummaryBean.isTestable()) {
                	this.vdbTestableMap.put(vdbSummaryBean.getName(), true);
                } else {
                	this.vdbTestableMap.put(vdbSummaryBean.getName(), false);
                }
            }
            this.vdbsTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }

    /**
     * Updates the columns pager with the given data.
     * @param data
     */
    protected void updatePager(VdbResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        
        long totalResults = data.getTotalResults();
        
        this.pager.setNumPages(numPages);
        this.pager.setPageSize(Constants.VDBS_TABLE_PAGE_SIZE);
        this.pager.setTotalItems(totalResults);
        
        // setPage is last - does render
        this.pager.setPage(thisPage);
        
        if(data.getVdbs().isEmpty()) {
        	this.pager.setVisible(false);
        } else {
        	this.pager.setVisible(true);
        }
    }

}
