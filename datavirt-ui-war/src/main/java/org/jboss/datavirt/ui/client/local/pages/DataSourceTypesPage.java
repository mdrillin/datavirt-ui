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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.events.TableRowSelectionEvent;
import org.jboss.datavirt.ui.client.local.pages.datasources.DataSourceTypesTable;
import org.jboss.datavirt.ui.client.local.pages.datasources.IImportCompletionHandler;
import org.jboss.datavirt.ui.client.local.pages.datasources.ImportDataSourceTypeDialog;
import org.jboss.datavirt.ui.client.local.pages.details.DeleteDataSourceTypeDialog;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateKeys;
import org.jboss.datavirt.ui.client.local.services.ApplicationStateService;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.Constants;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.NotificationBean;
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

/**
 * The default "Data Source Types" page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/datasourcetypes.html#page")
@Page(path="datasourcetypes")
@Dependent
public class DataSourceTypesPage extends AbstractPage {

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
 
    @Inject @DataField("btn-add-source-type")
    protected Button addSourceTypeButton;
    
    @Inject @DataField("btn-remove-source-type")
    protected Button removeSourceTypeButton;
    @Inject
    DeleteDataSourceTypeDialog deleteDataSourceTypeDialog;
    
    @Inject
    protected Instance<ImportDataSourceTypeDialog> importDataSourceTypeDialog;
    @Inject @DataField("btn-refresh")
    protected Button refreshButton;

    @Inject @DataField("datavirt-datasource-types-none")
    protected HtmlSnippet noDataMessage;
    @Inject @DataField("datavirt-datasource-types-searching")
    protected HtmlSnippet searchInProgressMessage;
    @Inject @DataField("datavirt-datasource-types-table")
    protected DataSourceTypesTable dataSourceTypesTable;

    @Inject @DataField("datavirt-datasource-types-pager")
    protected Pager pager;
    @DataField("datavirt-datasource-types-range-1")
    protected SpanElement rangeSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-datasource-types-total-1")
    protected SpanElement totalSpan1 = Document.get().createSpanElement();
    @DataField("datavirt-datasource-types-range-2")
    protected SpanElement rangeSpan2 = Document.get().createSpanElement();
    @DataField("datavirt-datasource-types-total-2")
    protected SpanElement totalSpan2 = Document.get().createSpanElement();

    private int currentPage = 1;
    
   /**
     * Constructor.
     */
    public DataSourceTypesPage() {
    }

    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
        pager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
            	doGetDataSourceTypes(event.getValue());
            }
        });
        dataSourceTypesTable.addTableSortHandler(new TableSortEvent.Handler() {
            @Override
            public void onTableSort(TableSortEvent event) {
            	doGetDataSourceTypes(currentPage);
            }
        });
        dataSourceTypesTable.addTableRowSelectionHandler(new TableRowSelectionEvent.Handler() {
            @Override
            public void onTableRowSelection(TableRowSelectionEvent event) {
                doSetButtonEnablements();
            }
        });
        deleteDataSourceTypeDialog.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onDeleteDataSourceTypeConfirm();
            }
        });

        this.rangeSpan1.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
        this.rangeSpan2.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
        this.totalSpan1.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
        this.totalSpan2.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
    }

    /**
     * Event handler that fires when the user clicks the AddSource button.
     * @param event
     */
    @EventHandler("btn-add-source-type")
    public void onAddSourceClick(ClickEvent event) {
        ImportDataSourceTypeDialog dialog = importDataSourceTypeDialog.get();
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
     * Event handler that fires when the user clicks the Remove source type button.
     * @param event
     */
    @EventHandler("btn-remove-source-type")
    public void onRemoveSourceClick(ClickEvent event) {
    	Collection<String> dsNames = dataSourceTypesTable.getSelectedDataSourceTypes();
        deleteDataSourceTypeDialog.setDataSourceTypeNames(dsNames);
        deleteDataSourceTypeDialog.show();
    }
        
    /**
     * Called when the user confirms the dataSource type deletion.
     */
    private void onDeleteDataSourceTypeConfirm() {
    	Collection<String> dsTypes = this.dataSourceTypesTable.getSelectedDataSourceTypes();
    	String dsText = null;
    	if(dsTypes.size()==1) {
    		dsText = "DataSource Type "+dsTypes.iterator().next();
    	} else {
    		dsText = "DataSource Type(s)";
    	}
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("datasource-types.deleting-datasource-type-title"), //$NON-NLS-1$
                i18n.format("datasource-types.deleting-datasource-type-msg", dsText)); //$NON-NLS-1$
        dataSourceService.deleteTypes(dsTypes, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("datasource-types.datasource-type-deleted"), //$NON-NLS-1$
                        i18n.format("datasource-types.delete-success-msg")); //$NON-NLS-1$

                // Deletion - go back to page 1 - delete could have made current page invalid
            	doGetDataSourceTypes(1);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("datasource-types.delete-error"), //$NON-NLS-1$
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
    	doGetDataSourceTypes(currentPage);
    }

    /**
     * Invoked on page showing
     *
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        Integer page = (Integer) stateService.get(ApplicationStateKeys.DATASOURCE_TYPES_PAGE, 1);
        SortColumn sortColumn = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCE_TYPES_SORT_COLUMN, dataSourceTypesTable.getDefaultSortColumn());

    	this.dataSourceTypesTable.sortBy(sortColumn.columnId, sortColumn.ascending);
    	
        // Kick off an dataSource retrieval
    	doGetDataSourceTypes(page);
    }

    private void doSetButtonEnablements() {    	
    	// Remove DataSource Button - enabled if at least one row is selected.
    	int selectedRows = this.dataSourceTypesTable.getSelectedDataSourceTypes().size();
    	if(selectedRows==0) {
    		removeSourceTypeButton.setEnabled(false);
    	} else {
    		removeSourceTypeButton.setEnabled(true);
    	}
    }
    
    /**
     * Search for Data Source Types based on the current filter settings and search text.
     */
    protected void doGetDataSourceTypes() {
    	doGetDataSourceTypes(1);
    }

    /**
     * Search for Data Source Types based on the current filter settings and search text.
     * @param page
     */
    protected void doGetDataSourceTypes(int page) {
        onSearchStarting();
        currentPage = page;
        final SortColumn currentSortColumn = this.dataSourceTypesTable.getCurrentSortColumn();

        stateService.put(ApplicationStateKeys.DATASOURCE_TYPES_PAGE, currentPage);
        stateService.put(ApplicationStateKeys.DATASOURCE_TYPES_SORT_COLUMN, currentSortColumn);
        
        dataSourceService.getDataSourceTypeResultSet(page, currentSortColumn.columnId, !currentSortColumn.ascending,
		        new IRpcServiceInvocationHandler<DataSourceTypeResultSetBean>() {
            @Override
            public void onReturn(DataSourceTypeResultSetBean data) {
                updateDataSourceTypesTable(data);
                updatePager(data);
                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("datasource-types.error-searching"), error); //$NON-NLS-1$
                noDataMessage.setVisible(true);
                searchInProgressMessage.setVisible(false);
            }
        });

    }

    /**
     * Called when a new Data Source Type retrieval is kicked off.
     */
    protected void onSearchStarting() {
        this.pager.setVisible(false);
        this.searchInProgressMessage.setVisible(true);
        this.dataSourceTypesTable.setVisible(false);
        this.noDataMessage.setVisible(false);
        this.rangeSpan1.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
        this.rangeSpan2.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
        this.totalSpan1.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
        this.totalSpan2.setInnerText(Constants.QUESTION_MARK); //$NON-NLS-1$
    }

    /**
     * Updates the table of Data Source Types with the given data.
     * @param data
     */
    protected void updateDataSourceTypesTable(DataSourceTypeResultSetBean data) {
        this.dataSourceTypesTable.clear();
        this.searchInProgressMessage.setVisible(false);
        if (data.getDataSourceTypes().size() > 0) {
            for (DataSourceTypeBean dataSourceTypeBean : data.getDataSourceTypes()) {
                this.dataSourceTypesTable.addRow(dataSourceTypeBean);
            }
            this.dataSourceTypesTable.setVisible(true);
        } else {
            this.noDataMessage.setVisible(true);
        }
    }

    /**
     * Updates the pager with the given data.
     * @param data
     */
    protected void updatePager(DataSourceTypeResultSetBean data) {
        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
        this.pager.setNumPages(numPages);
        this.pager.setPage(thisPage);
        if (numPages > 1)
            this.pager.setVisible(true);

        int startIndex = data.getStartIndex();
        int endIndex = startIndex + data.getDataSourceTypes().size();
        
        // reset start index to zero if end index is zero
        startIndex = (endIndex==0) ? endIndex : startIndex+1;
        
        
        String rangeText = "" + startIndex + "-" + endIndex; //$NON-NLS-1$ //$NON-NLS-2$
        String totalText = String.valueOf(data.getTotalResults());
        this.rangeSpan1.setInnerText(rangeText);
        this.rangeSpan2.setInnerText(rangeText);
        this.totalSpan1.setInnerText(totalText);
        this.totalSpan2.setInnerText(totalText);
    }

}
