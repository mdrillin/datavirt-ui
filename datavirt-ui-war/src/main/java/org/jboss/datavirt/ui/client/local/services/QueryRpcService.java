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
package org.jboss.datavirt.ui.client.local.services;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.services.rpc.DelegatingErrorCallback;
import org.jboss.datavirt.ui.client.local.services.rpc.DelegatingRemoteCallback;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.QueryColumnResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.QueryTableProcBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.jboss.datavirt.ui.client.shared.services.IQueryService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * Client-side service for making RPC calls to the remote Query service.
 *
 * @author mdrillin@redhat.com
 */
@ApplicationScoped
public class QueryRpcService {

    @Inject
    private Caller<IQueryService> remoteQueryService;

    /**
     * Constructor.
     */
    public QueryRpcService() {
    }

    public void getDataSourceNames(final boolean teiidOnly, final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getDataSourceNames(teiidOnly);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getTablesAndProcedures(final String sourceName, final IRpcServiceInvocationHandler<List<QueryTableProcBean>> handler) {
        RemoteCallback<List<QueryTableProcBean>> successCallback = new DelegatingRemoteCallback<List<QueryTableProcBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getTablesAndProcedures(sourceName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getQueryColumnResultSet(int page, String dataSource, String fullTableName,
            final IRpcServiceInvocationHandler<QueryColumnResultSetBean> handler) {
        // TODO only allow one search at a time.  If another search comes in before the previous one
        // finished, cancel the previous one.  In other words, only return the results of the *last*
        // search performed.
        RemoteCallback<QueryColumnResultSetBean> successCallback = new DelegatingRemoteCallback<QueryColumnResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).getQueryColumnResultSet(page, dataSource, fullTableName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void executeSql(int page, String dataSource, String sql, final IRpcServiceInvocationHandler<QueryResultSetBean> handler) {
        // TODO only allow one search at a time.  If another search comes in before the previous one
        // finished, cancel the previous one.  In other words, only return the results of the *last*
        // search performed.
        RemoteCallback<QueryResultSetBean> successCallback = new DelegatingRemoteCallback<QueryResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteQueryService.call(successCallback, errorCallback).executeSql(page, dataSource, sql);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

}
