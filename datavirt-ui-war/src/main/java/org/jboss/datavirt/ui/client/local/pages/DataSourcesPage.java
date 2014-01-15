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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.pages.datasources.AddDataSourceDialog;
import org.jboss.datavirt.ui.client.local.pages.datasources.DataSourcesTable;
import org.jboss.datavirt.ui.client.local.pages.details.DeleteDataSourceDialog;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceSummaryBean;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
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
 * The default "DataSources" page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/datasources.html#page")
@Page(path="datasources", role=DefaultPage.class)
@Dependent
public class DataSourcesPage extends AbstractPage {

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
    @Inject
    protected ApplicationStateService stateService;
 
    @Inject @DataField("datasource-search-box")
    protected TextBox searchBox;

    @Inject @DataField("btn-add-source")
    protected Button addSourceButton;
    
    @Inject @DataField("btn-remove-source")
    protected Button removeSourceButton;
    @Inject
    DeleteDataSourceDialog deleteDataSourceDialog;
    
    @Inject @DataField("btn-test-source")
    protected Button testSourceButton;
    
    @Inject
    protected Instance<AddDataSourceDialog> addDataSourceDialogFactory;
    @Inject @DataField("btn-refresh")
    protected Button refreshButton;

    @Inject @DataField("datavirt-datasources-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("datavirt-datasources-searching")
    protected HtmlSnippet searchInProgressMessage;
    @Inject @DataField("datavirt-datasources-table")
    protected DataSourcesTable dataSourcesTable;

    @Inject @DataField("datavirt-datasources-pager")
    protected Pager pager;
    @DataField("datavirt-datasources-range-1")
    protected SpanElement rangeSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-datasources-total-1")
    protected SpanElement totalSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-datasources-range-2")
    protected SpanElement rangeSpan2 = Document.get().createSpanElement();
    @DataField("datavirt-datasources-total-2")
    protected SpanElement totalSpan2 = Document.get().createSpanElement();

    private int currentPage = 1;
    private Collection<String> allDsNames = new ArrayList<String>();
    
   /**
     * Constructor.
     */
    public DataSourcesPage() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                doDataSourceSearch();
            }
        });
        pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
            	doDataSourceSearch(event.getValue());
            }
        });
        dataSourcesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
                doDataSourceSearch(currentPage);
            }
        });
        dataSourcesTable.addTableRowSelectionHandler(new TableRowSelectionEvent.Handler() {
            @Override
            public void onTableRowSelection(TableRowSelectionEvent event) {
                doSetButtonEnablements();
            }
        });
        deleteDataSourceDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteDataSourceConfirm();
            }
        });

        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }

    /**
     * Event handler that fires when the user clicks the AddSource button.
     * @param event
     */
    @EventHandler("btn-add-source")
    public void onAddSourceClick(ClickEvent event) {
        AddDataSourceDialog dialog = addDataSourceDialogFactory.get();
        dialog.setCurrentDsNames(this.allDsNames);
        dialog.addValueChangeHandler(new ValueChangeHandler<Map<String,String>>() {
        	@Override
        	public void onValueChange(ValueChangeEvent<Map<String, String>> event) {
//        		Properties dsProps = new Properties();
//        		// Returns Map of all (propName, propValue) pairs where the value is not the default
//        		Map<String, String> propNameValueMap = event.getValue();
//
//        		// Create the Data Source
//        		String dsName = "test";
//        		String dsType = "salesforce";
//        		doCreateDataSource(dsName,dsType,propNameValueMap);
        	}
        });
        dialog.show();
    }
    
    /**
     * Creates a DataSource
     * @param dsName the data source name
     * @param dsType the data source template
     * @param dsProps the data source propertis
     */
//    private void doCreateDataSource(String dsName, String dsType, Map<String,String> dsPropMap) {
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("datasources.creating-datasource-title"), //$NON-NLS-1$
//                i18n.format("datasources.creating-datasource-msg", dsName)); //$NON-NLS-1$
//        dataSourceService.createDataSource(dsName, dsType, dsPropMap, new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.datasource-created"), //$NON-NLS-1$
//                        i18n.format("datasources.create-success-msg")); //$NON-NLS-1$
//
//                // Refresh Page
//            	doDataSourceSearch(currentPage);
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.create-error"), //$NON-NLS-1$
//                        error);
//            }
//        });
//    }
    
    /**
     * Event handler that fires when the user clicks the RemoveSource button.
     * @param event
     */
    @EventHandler("btn-remove-source")
    public void onRemoveSourceClick(ClickEvent event) {
    	Collection<String> dsNames = dataSourcesTable.getSelectedDataSources();
        deleteDataSourceDialog.setDataSourceNames(dsNames);
        deleteDataSourceDialog.show();
    }
        
    /**
     * Called when the user confirms the dataSource deletion.
     */
    private void onDeleteDataSourceConfirm() {
    	Collection<String> dsNames = this.dataSourcesTable.getSelectedDataSources();
    	String dsText = null;
    	if(dsNames.size()==1) {
    		dsText = "DataSource "+dsNames.iterator().next();
    	} else {
    		dsText = "DataSource(s)";
    	}
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("datasources.deleting-datasource-title"), //$NON-NLS-1$
                i18n.format("datasources.deleting-datasource-msg", dsText)); //$NON-NLS-1$
        dataSourceService.deleteDataSources(dsNames, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("datasources.datasourc-deleted"), //$NON-NLS-1$
                        i18n.format("datasources.delete-success-msg")); //$NON-NLS-1$

                // Refresh Page
            	doDataSourceSearch(currentPage);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("datasources.delete-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /**
     * Event handler that fires when the user clicks the Test button.
     * @param event
     */
    @EventHandler("btn-test-source")
    public void onTestSourceClick(ClickEvent event) {
    }

    /**
     * Event handler that fires when the user clicks the refresh button.
     * @param event
     */
    @EventHandler("btn-refresh")
    public void onRefreshClick(ClickEvent event) {
    	doDataSourceSearch(currentPage);
    }

    /**
     * Invoked on page showing
     *
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        String searchText = (String) stateService.get(ApplicationStateKeys.DATASOURCES_SEARCH_TEXT, ""); //$NON-NLS-1$
        Integer page = (Integer) stateService.get(ApplicationStateKeys.DATASOURCES_PAGE, 1);
        SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, dataSourcesTable.getDefaultSortColumn());

    	this.searchBox.setValue(searchText);
    	this.dataSourcesTable.sortBy(sortColumn.columnId, sortColumn.ascending);
    	
        // Kick off an dataSource retrieval
    	doDataSourceSearch(page);
    }

    private void doSetButtonEnablements() {
    	// Test Button Disabled for now
    	testSourceButton.setEnabled(false);
    	
    	// Remove DataSource Button - enabled if at least one row is selected.
    	int selectedRows = this.dataSourcesTable.getSelectedDataSources().size();
    	if(selectedRows==0) {
    		removeSourceButton.setEnabled(false);
    	} else {
    		removeSourceButton.setEnabled(true);
    	}
    }
    
    /**
     * Search for the datasources based on the current filter settings and search text.
     */
    protected void doDataSourceSearch() {
    	doDataSourceSearch(1);
    }

    /**
     * Search for Data Sources based on the current filter settings and search text.
     * @param page
     */
    protected void doDataSourceSearch(int page) {
        onSearchStarting();
        currentPage = page;
		final String searchText = this.searchBox.getValue();
        final SortColumn currentSortColumn = this.dataSourcesTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.DATASOURCES_SEARCH_TEXT, searchText);
        stateService.put(ApplicationStateKeys.DATASOURCES_PAGE, currentPage);
        stateService.put(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, currentSortColumn);
        
        dataSourceService.search(searchText, page, currentSortColumn.columnId, currentSortColumn.ascending,
		        new IRpcServiceInvocationHandler<DataSourceResultSetBean>() {
            @Override
            public void onReturn(DataSourceResultSetBean data) {
            	allDsNames = data.getAllDsNames();
                updateDataSourcesTable(data);
                updatePager(data);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                searchInProgressMessage.setVisible(false);
            }
        });

    }

    /**
     * Called when a new Data Source search is kicked off.
     */
    protected void onSearchStarting() {
        this.pager.setVisible(false);
        this.searchInProgressMessage.setVisible(true);
        this.dataSourcesTable.setVisible(false);
        this.noDataMessage.setVisible(false);
        this.rangeSpan1.setInnerText("?"); //$NON-NLS-1$
        this.rangeSpan2.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan1.setInnerText("?"); //$NON-NLS-1$
        this.totalSpan2.setInnerText("?"); //$NON-NLS-1$
    }

    /**
     * Updates the table of Data Sources with the given data.
     * @param data
     */
    protected void updateDataSourcesTable(DataSourceResultSetBean data) {
        this.dataSourcesTable.clear();
        this.searchInProgressMessage.setVisible(false);
        if (data.getDataSources().size() > 0) {
            for (DataSourceSummaryBean dataSourceSummaryBean : data.getDataSources()) {
                this.dataSourcesTable.addRow(dataSourceSummaryBean);
            }
            this.dataSourcesTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }

    /**
     * Updates the pager with the given data.
     * @param data
     */
    protected void updatePager(DataSourceResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        this.pager.setNumPages(numPages);
        this.pager.setPage(thisPage);
        if (numPages > 1)
            this.pager.setVisible(true);

        int startIndex = data.getStartIndex() + 1;
        int endIndex = startIndex + data.getDataSources().size() - 1;
        String rangeText = "" + startIndex + "-" + endIndex; //$NON-NLS-1$ //$NON-NLS-2$
        String totalText = String.valueOf(data.getTotalResults());
        this.rangeSpan1.setInnerText(rangeText);
        this.rangeSpan2.setInnerText(rangeText);
        this.totalSpan1.setInnerText(totalText);
        this.totalSpan2.setInnerText(totalText);
    }

}
