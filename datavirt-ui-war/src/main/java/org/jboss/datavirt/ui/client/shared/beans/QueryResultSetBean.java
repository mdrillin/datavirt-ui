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
package org.jboss.datavirt.ui.client.shared.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models the set of Query results returned by a query execution.
 *
 * @author mdrillin@redhat.com
 */
@Portable
public class QueryResultSetBean implements Serializable {

    private static final long serialVersionUID = QueryResultSetBean.class.hashCode();

    private List<QueryResultRowBean> resultRows = new ArrayList<QueryResultRowBean>();
    private List<String> columnNames = new ArrayList<String>();
    private List<String> columnTypes = new ArrayList<String>();
    
    private long totalResults;
    private int itemsPerPage;
    private int startIndex;

    /**
     * Constructor.
     */
    public QueryResultSetBean() {
    }

    /**
     * @return the Query result rows
     */
    public List<QueryResultRowBean> getResultRows() {
        return resultRows;
    }

    /**
     * @return the Query result column names
     */
    public List<String> getResultColumnNames() {
        return columnNames;
    }

    /**
     * @return the Query result column names
     */
    public List<String> getResultColumnTypes() {
        return columnTypes;
    }

    /**
     * @param resultRows the query result rows to set
     */
    public void setResultRows(List<QueryResultRowBean> resultRows) {
        this.resultRows = resultRows;
    }
    
    /**
     * @param columnNames the query columnNames to set
     */
    public void setResultColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    /**
     * @param columnTypes the query columnTypes to set
     */
    public void setResultColumnTypes(List<String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    /**
     * @return the totalResults
     */
    public long getTotalResults() {
        return totalResults;
    }

    /**
     * @param totalResults the totalResults to set
     */
    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * @return the itemsPerPage
     */
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    /**
     * @param itemsPerPage the itemsPerPage to set
     */
    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * @return the startIndex
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

}
