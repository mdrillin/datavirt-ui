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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.pages.datasources.DVPager;
import org.jboss.datavirt.ui.client.local.pages.query.QueryColumnsTable;
import org.jboss.datavirt.ui.client.local.pages.query.QueryResultTable;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.QueryRpcService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.QueryColumnBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryColumnResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryResultRowBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryTableProcBean;
import org.jboss.datavirt.ui.client.shared.services.StringUtils;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The default "QueryTest" page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/querytest.html#page")
@Page(path="querytest")
@Dependent
public class QueryTestPage extends AbstractPage {

    @Inject @DataField("to-datasources-page")
    private TransitionAnchor<DataSourcesPage> toDataSourcesPage;
    @Inject @DataField("to-vdbs-page")
    private TransitionAnchor<VirtualDatabasesPage> toVDBsPage;
    @Inject @DataField("to-querytest-page")
    private TransitionAnchor<QueryTestPage> toQueryTestPage;

    @Inject
    protected ClientMessages i18n;
    @Inject
    protected DataSourceRpcService dataSourceService;
    @Inject
    protected QueryRpcService queryService;
    @Inject
    protected NotificationService notificationService;
    @Inject
    protected ApplicationStateService stateService;
 
    @Inject @DataField("select-source-listbox")
    protected ListBox sourceListBox;
    @Inject @DataField("select-table-listbox")
    protected ListBox tablesListBox;
    
    @Inject @DataField("query-textarea")
    protected TextArea queryTextArea;
    
    @Inject @DataField("query-submit-button")
    protected Button querySubmitButton;
    
    @Inject @DataField("columns-filter-box")
    protected TextBox columnsFilterBox;
    @Inject @DataField("columns-pager")
    protected DVPager columnsPager;

    @Inject @DataField("columns-none")
    protected HtmlSnippet noColumnsMessage;
    @Inject @DataField("columns-fetching")
    protected HtmlSnippet columnFetchInProgressMessage;
    @Inject @DataField("columns-table")
    protected QueryColumnsTable columnsTable;

    @Inject @DataField("query-results-pager")
    protected DVPager queryResultsPager;

    @Inject @DataField("query-results-execute-to-fetch")
    protected HtmlSnippet queryResultsMessageExecuteToFetch;
    @Inject @DataField("query-results-none")
    protected HtmlSnippet queryResultsMessageNoResults;
    @Inject @DataField("query-results-fetching")
    protected HtmlSnippet queryResultsMessageFetchInProgress;
    @Inject @DataField("query-results-table")
    protected QueryResultTable queryResultsTable;

    private static final String NO_SOURCE_SELECTION = "[Select a source]"; 
    private static final String NO_TABLES_FOUND = "[No tables found]"; 
    private static final String NO_TABLE_SELECTION = "[Select a table]"; 
    
    private int currentQueryColumnsPage = 1;
    private int currentQueryResultsPage = 1;
    private Map<String,String> tableTypeMap = new HashMap<String,String>();
    private Map<String,String> sourceNameToJndiMap = new HashMap<String,String>();
    
    /**
     * Constructor.
     */
    public QueryTestPage() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        querySubmitButton.setEnabled(false);

        columnsFilterBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	filterColumnsTable();
            }
        });
        
        columnsPager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
            	doGetTableColumns(event.getValue());
            }
        });

        queryResultsPager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
            	doSubmitQuery(event.getValue());
            }
        });
        
        // Change Listener for DataSource ListBox
        sourceListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the DataSource selection will set the best guess translator and default the model name if empty
        	public void onChange(ChangeEvent event)
        	{
        		// Clear SQL Text
        		clearQueryTextArea();
        		
        		// Clear query results
        		clearQueryResultsTable();
        		
        		// Populate the Tables ListBox
        		String selectedSource = getSelectedSource();
        		doPopulateTablesListBox(selectedSource);
        		
        		clearColumnsTable();
        		clearQueryResultsTable();
        	}
        });

        // Change Listener for Select Table ListBox
        tablesListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		// Clear query results
        		clearQueryResultsTable();

        		// Reset filter on Table selection changed
        		columnsFilterBox.setText("");
                stateService.put(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT, "");

        		// Populate TableColumns table
            	doGetTableColumns();
            	
            	// Update SQL area
            	refreshSQLTextArea();
            	
            	// Set Submit Button Enabled state
            	setSubmitButtonState();
        	}
        });

        queryTextArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	clearQueryResultsTable();
            }
        });
    }

    /**
     * Invoked on page showing.
     *
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        doPopulateSourceListBox(false);
        doPopulateTablesListBox(NO_SOURCE_SELECTION);
        clearColumnsTable();
        clearQueryResultsTable();
    }
    
    /**
     * Event handler that fires when the user clicks the submit query button.
     * @param event
     */
    @EventHandler("query-submit-button")
    public void onQuerySubmitClick(ClickEvent event) {
    	doSubmitQuery();
    }
    
    /**
     * Populate the DataSource ListBox
     */
    protected void doPopulateSourceListBox(boolean teiidOnly) {
        dataSourceService.getQueryableDataSourceMap(new IRpcServiceInvocationHandler<Map<String,String>>() {
            @Override
            public void onReturn(Map<String,String> sourceToJndiMap) {
            	sourceNameToJndiMap.clear();
            	sourceNameToJndiMap.putAll(sourceToJndiMap);
                populateSourceListBox(sourceToJndiMap.keySet());
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("queryTest.error-populating-sources"), error); //$NON-NLS-1$
            }
        });
//        queryService.getDataSourceNames(teiidOnly, new IRpcServiceInvocationHandler<List<String>>() {
//            @Override
//            public void onReturn(List<String> sources) {
//                populateSourceListBox(sources);
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("queryTest.error-populating-sources"), error); //$NON-NLS-1$
//            }
//        });
    }

    /**
     * Populate the Tables and Procedures ListBox
     */
    protected void doPopulateTablesListBox(String sourceName) {
    	if(sourceName!=null && sourceName.equalsIgnoreCase(NO_SOURCE_SELECTION)) {
    		List<QueryTableProcBean> noTables = Collections.emptyList();
    		populateTablesListBox(noTables);
    	} else {
    		queryService.getTablesAndProcedures(sourceNameToJndiMap.get(sourceName), new IRpcServiceInvocationHandler<List<QueryTableProcBean>>() {
    			@Override
    			public void onReturn(List<QueryTableProcBean> tablesAndProcs) {
    				populateTablesListBox(tablesAndProcs);
    			}
    			@Override
    			public void onError(Throwable error) {
    				notificationService.sendErrorNotification(i18n.format("queryTest.error-populating-tables"), error); //$NON-NLS-1$
    			}
    		});
    	}
    }

    /*
     * Init the List of DataSource Template Names
     * @param vdbName the name of the VDB
     * @param sourceName the source name
     * @param templateName the template name
     * @param translatorName the translator name
     * @param propsMap the property Map of name-value pairs
     */
    private void populateSourceListBox(Set<String> sources) {
    	// Make sure clear first
    	sourceListBox.clear();

    	sourceListBox.insertItem(NO_SOURCE_SELECTION, 0);
    	
    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String source: sources) {
    		sourceListBox.insertItem(source, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	sourceListBox.setSelectedIndex(0);
    }
    
    /*
     * Init the List of DataSource Template Names
     * @param vdbName the name of the VDB
     * @param sourceName the source name
     * @param templateName the template name
     * @param translatorName the translator name
     * @param propsMap the property Map of name-value pairs
     */
    private void populateTablesListBox(List<QueryTableProcBean> tablesAndProcs) {
    	// Make sure clear first
    	tablesListBox.clear();
    	// Update mapping of name to type (table or proc)
    	updateTableTypeMap(tablesAndProcs);
    	
    	if(tablesAndProcs.isEmpty()) {
    		tablesListBox.insertItem(NO_TABLES_FOUND, 0);
    	} else {
    		tablesListBox.insertItem(NO_TABLE_SELECTION, 0);
        	// populate the ListBox.
        	int i = 1;
        	for(QueryTableProcBean tableProc: tablesAndProcs) {
        		tablesListBox.insertItem(tableProc.getName(), i);
        		i++;
        	}
    	}

    	// Initialize by setting the selection to the first item.
    	tablesListBox.setSelectedIndex(0);
    }
    
    private void updateTableTypeMap(List<QueryTableProcBean> tablesAndProcs) {
    	tableTypeMap.clear();
    	for(QueryTableProcBean tableProc : tablesAndProcs) {
    		tableTypeMap.put(tableProc.getName(),tableProc.getType());
    	}
    }

    private void clearQueryTextArea() {
    	queryTextArea.setText("");
    	querySubmitButton.setEnabled(false);
    }
    
    private void setSubmitButtonState() {
    	boolean enable = false;
    	String sqlText = queryTextArea.getText();
    	if(sqlText!=null && !sqlText.trim().equalsIgnoreCase("SELECT * FROM") && !sqlText.contains("<params>")) {
    		enable = true;
    	}
    	querySubmitButton.setEnabled(enable);
    }
    
    /**
     * Get the selected Source Name from the Source ListBox
     * @return the Source Name
     */
    public String getSelectedSource( ) {
    	int selectedIndex = sourceListBox.getSelectedIndex();
    	if(selectedIndex<0) selectedIndex=0;
    	return sourceListBox.getItemText(selectedIndex);
    }
    
    /**
     * Get the selected Table Name from the Table ListBox
     * @return the Table Name
     */
    public String getSelectedTable( ) {
    	int selectedIndex = tablesListBox.getSelectedIndex();
    	if(selectedIndex<0) selectedIndex=0;
    	return tablesListBox.getItemText(selectedIndex);
    }
    
    /**
     * Filter the columns table if filterBox text is different than application state
     */
    private void filterColumnsTable() {
    	// Current Filter Text state
        String appFilterText = (String) stateService.get(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT, ""); //$NON-NLS-1$
        // SearchBox text
        String filterColsText = this.columnsFilterBox.getText();
        // Search if different
        if(!StringUtils.equals(appFilterText, filterColsText)) {
        	stateService.put(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT, filterColsText);
        	doGetTableColumns();
        }    	
    }
        
    protected void doGetTableColumns() {
    	// Resets queryCols page
    	stateService.put(ApplicationStateKeys.QUERY_COLUMNS_PAGE, 1);
    	
        doGetTableColumns(1);
    }
    
    /**
     * Search for QueryColumns based on the current page and filter settings.
     * @param page
     */
    protected void doGetTableColumns(int page) {
    	onQueryColumnsFetchStarting();
    	currentQueryColumnsPage = page;

    	String filterText = (String)stateService.get(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT,"");
        stateService.put(ApplicationStateKeys.QUERY_COLUMNS_PAGE, currentQueryColumnsPage);
        
        String source = getSelectedSource();
        String table = getSelectedTable();
        
        if(table!=null && table.equalsIgnoreCase(NO_TABLES_FOUND)) {
        	clearColumnsTable();
        } else {
        	queryService.getQueryColumnResultSet(page, filterText, sourceNameToJndiMap.get(source), table,
        			new IRpcServiceInvocationHandler<QueryColumnResultSetBean>() {
        		@Override
        		public void onReturn(QueryColumnResultSetBean data) {
        			updateQueryColumnsTable(data);
        			updateQueryColumnsPager(data);
        		}
        		@Override
        		public void onError(Throwable error) {
        			notificationService.sendErrorNotification(i18n.format("queryTest.error-fetching-columns"), error); //$NON-NLS-1$
        		    noColumnsMessage.setVisible(true);
        		    columnFetchInProgressMessage.setVisible(false);
        		}
        	});
        }

    }
    
    /*
     * Clears the ColumnsTable
     */
    private void clearColumnsTable() {
    	QueryColumnResultSetBean resultSetBean = new QueryColumnResultSetBean();
    	resultSetBean.setTotalResults(0);
    	resultSetBean.setItemsPerPage(7);
    	resultSetBean.setStartIndex(0);
    	updateQueryColumnsTable(resultSetBean);
    	updateQueryColumnsPager(resultSetBean);
    }

    /*
     * Clears the Query Results Table
     */
    private void clearQueryResultsTable() {
    	QueryResultSetBean resultSetBean = new QueryResultSetBean();
    	resultSetBean.setTotalResults(0);
    	resultSetBean.setItemsPerPage(7);
    	resultSetBean.setStartIndex(0);
    	updateQueryResultsTable(resultSetBean,true);
    	updateQueryResultsPager(resultSetBean);
    }
    
    /*
     * Refresh the SQL Text area, using the current Tables
     * ListBox item to generate "SELECT * FROM <Table>"
     * the supplied dataSource, and sets the SQL Area Text
     */
    private void refreshSQLTextArea( ) {
    	// Get the Table. If there is a selected table, use it.
    	// Otherwise, get the first item in the list.
    	String selectedTable = null;
    	int tableIndex = tablesListBox.getSelectedIndex();
    	if(tablesListBox.getSelectedIndex()>=0) {
    		selectedTable = tablesListBox.getItemText(tableIndex);
    	} else {
    		selectedTable = tablesListBox.getItemText(0);
    	}

    	// Determine if the selection is a Table or Procedure
    	String type = tableTypeMap.get(selectedTable);
    	StringBuffer sb = new StringBuffer();
    	if(type==null || type.equals(QueryTableProcBean.TABLE)) {
    		sb.append("SELECT * FROM ");
    		if( !selectedTable.equals(NO_TABLES_FOUND) && !selectedTable.equals(NO_TABLE_SELECTION)) {
    			sb.append(selectedTable);
    		}
    	} else if(type.equals(QueryTableProcBean.PROCEDURE)) {
    		sb.append("SELECT * FROM (EXEC ");
    		sb.append(selectedTable);
    		sb.append("(<params>)) AS Result");
    	}

    	queryTextArea.setText(sb.toString());
    }

    protected void doSubmitQuery() {
    	// Resets queryResults page
    	stateService.put(ApplicationStateKeys.QUERY_RESULTS_PAGE, 1);
    	
    	doSubmitQuery(1);
    }
    
    /*
     * Handler for Submit Button Pressed
     */
    private void doSubmitQuery(int page) {
    	onQueryResultsFetchStarting();
    	currentQueryResultsPage = page;

        stateService.put(ApplicationStateKeys.QUERY_RESULTS_PAGE, currentQueryResultsPage);

        // Get the selected source
    	String source = getSelectedSource();

    	// Get SQL
    	String sql = queryTextArea.getText();

    	queryService.executeSql(page, sourceNameToJndiMap.get(source), sql,
    			new IRpcServiceInvocationHandler<QueryResultSetBean>() {
    		@Override
    		public void onReturn(QueryResultSetBean data) {
    			updateQueryResultsTable(data,false);
    			updateQueryResultsPager(data);
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.sendErrorNotification(i18n.format("queryTest.error-executing-query"), error); //$NON-NLS-1$
    		    queryResultsMessageExecuteToFetch.setVisible(true);
    		    queryResultsMessageNoResults.setVisible(false);
    		    queryResultsMessageFetchInProgress.setVisible(false);
    		}
    	});
    }
    
    /**
     * Called when a new Data Source search is kicked off.
     */
    protected void onQueryColumnsFetchStarting() {
        this.columnsPager.setVisible(false);
        this.columnFetchInProgressMessage.setVisible(true);
        this.columnsTable.setVisible(false);
        this.noColumnsMessage.setVisible(false);
    }

    /**
     * Called when a new Data Source search is kicked off.
     */
    protected void onQueryResultsFetchStarting() {
        this.queryResultsPager.setVisible(false);
        this.queryResultsTable.setVisible(false);

	    queryResultsMessageExecuteToFetch.setVisible(false);
	    queryResultsMessageNoResults.setVisible(false);
	    queryResultsMessageFetchInProgress.setVisible(true);
    }
    
    /**
     * Updates the table of QueryColumns with the given data.
     * @param data
     */
    protected void updateQueryColumnsTable(QueryColumnResultSetBean data) {
        this.columnsTable.clear();
        this.columnFetchInProgressMessage.setVisible(false);
        if (data.getQueryColumns().size() > 0) {
            for (QueryColumnBean queryColBean : data.getQueryColumns()) {
                this.columnsTable.addRow(queryColBean);
            }
            this.columnsTable.setVisible(true);
        } else {
            this.noColumnsMessage.setVisible(true);
            this.columnsTable.setVisible(false);
        }
    }
    
    /**
     * Updates the Columns table pager with the given data.
     * @param data
     */
    protected void updateQueryColumnsPager(QueryColumnResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        
        long totalResults = data.getTotalResults();
        
        this.columnsPager.setNumPages(numPages);
        this.columnsPager.setPageSize(Constants.QUERY_COLUMNS_TABLE_PAGE_SIZE);
        this.columnsPager.setTotalItems(totalResults);
        
        // setPage is last - does render
        this.columnsPager.setPage(thisPage);
        
        if(data.getQueryColumns().isEmpty()) {
        	this.columnsPager.setVisible(false);
        } else {
        	this.columnsPager.setVisible(true);
        }
    }

    /**
     * Updates the table of QueryColumns with the given data.
     * @param data
     */
    protected void updateQueryResultsTable(QueryResultSetBean data, boolean showInitMessageForNoRows) {
    	this.queryResultsTable.clear();
        this.queryResultsMessageFetchInProgress.setVisible(false);
        if (data.getResultRows().size() > 0) {
        	List<String> colNames = data.getResultColumnNames();
        	String[] colArray = colNames.toArray(new String[colNames.size()]);
        	
        	this.queryResultsTable.setColumnLabels(colArray);
        	
            for (QueryResultRowBean queryRowBean : data.getResultRows()) {
                this.queryResultsTable.addRow(queryRowBean);
            }
            this.queryResultsTable.setVisible(true);
        } else {
        	// Initial Message
        	if(showInitMessageForNoRows) {
        	    queryResultsMessageNoResults.setVisible(false);
        	    queryResultsMessageExecuteToFetch.setVisible(true);
        	// Message after query execution with no rows
        	} else {
        	    queryResultsMessageExecuteToFetch.setVisible(false);
        	    queryResultsMessageNoResults.setVisible(true);
        	}
            this.queryResultsTable.setVisible(false);
        }
    }
    
    /**
     * Updates the Query results table pager with the given data.
     * @param data
     */
    protected void updateQueryResultsPager(QueryResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        
        long totalResults = data.getTotalResults();
        
        this.queryResultsPager.setNumPages(numPages);
        this.queryResultsPager.setPageSize(Constants.QUERY_RESULTS_TABLE_PAGE_SIZE);
        this.queryResultsPager.setTotalItems(totalResults);
        
        // setPage is last - does render
        this.queryResultsPager.setPage(thisPage);
        
        if(data.getResultRows().isEmpty()) {
        	this.queryResultsPager.setVisible(false);
        } else {
        	this.queryResultsPager.setVisible(true);
        }
    }

}
