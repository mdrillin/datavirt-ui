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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.services.rpc.DelegatingErrorCallback;
import org.jboss.datavirt.ui.client.local.services.rpc.DelegatingRemoteCallback;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceResultSetBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceTypeResultSetBean;
import org.jboss.datavirt.ui.client.shared.exceptions.DataVirtUiException;
import org.jboss.datavirt.ui.client.shared.services.IDataSourceService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

/**
 * Client-side service for making RPC calls to the remote DataSource service.
 *
 * @author mdrillin@redhat.com
 */
@ApplicationScoped
public class DataSourceRpcService {

    @Inject
    private Caller<IDataSourceService> remoteDataSourceService;

    /**
     * Constructor.
     */
    public DataSourceRpcService() {
    }

    /**
     * Performs the search using the remote service.  Hides the RPC details from
     * the caller.
     * @param searchText
     * @param page
     * @param sortColumnId
     * @param sortAscending
     * @param handler
     */
    public void search(String searchText, int page, String sortColumnId, boolean sortAscending,
            final IRpcServiceInvocationHandler<DataSourceResultSetBean> handler) {
        // TODO only allow one search at a time.  If another search comes in before the previous one
        // finished, cancel the previous one.  In other words, only return the results of the *last*
        // search performed.
        RemoteCallback<DataSourceResultSetBean> successCallback = new DelegatingRemoteCallback<DataSourceResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).search(searchText, page, sortColumnId, sortAscending);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDataSourceDetails(String dsName, final IRpcServiceInvocationHandler<DataSourceDetailsBean> handler) {
        RemoteCallback<DataSourceDetailsBean> successCallback = new DelegatingRemoteCallback<DataSourceDetailsBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getDataSourceDetails(dsName);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void getDataSourceTypes(final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getDataSourceTypes();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDataSources(final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getDataSources();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getTranslators(final IRpcServiceInvocationHandler<List<String>> handler) {
        RemoteCallback<List<String>> successCallback = new DelegatingRemoteCallback<List<String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getTranslators();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDefaultTranslatorMap(final IRpcServiceInvocationHandler<Map<String,String>> handler) {
        RemoteCallback<Map<String,String>> successCallback = new DelegatingRemoteCallback<Map<String,String>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getDefaultTranslatorMap();
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void getDataSourceTypeResultSet(int page, String sortColumnId, boolean sortAscending, 
    		final IRpcServiceInvocationHandler<DataSourceTypeResultSetBean> handler) {
        RemoteCallback<DataSourceTypeResultSetBean> successCallback = new DelegatingRemoteCallback<DataSourceTypeResultSetBean>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getDataSourceTypeResultSet(page,sortColumnId,sortAscending);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void getDataSourceTypeProperties(String dsType, final IRpcServiceInvocationHandler<List<DataSourcePropertyBean>> handler) {
        RemoteCallback<List<DataSourcePropertyBean>> successCallback = new DelegatingRemoteCallback<List<DataSourcePropertyBean>>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).getDataSourceTypeProperties(dsType);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void update(DataSourceDetailsBean dataSource, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).update(dataSource);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

    public void deleteDataSources(Collection<String> dsNames, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).deleteDataSources(dsNames);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }
    
    public void deleteTypes(Collection<String> dsTypes, final IRpcServiceInvocationHandler<Void> handler) {
        RemoteCallback<Void> successCallback = new DelegatingRemoteCallback<Void>(handler);
        ErrorCallback<?> errorCallback = new DelegatingErrorCallback(handler);
        try {
        	remoteDataSourceService.call(successCallback, errorCallback).deleteTypes(dsTypes);
        } catch (DataVirtUiException e) {
            errorCallback.error(null, e);
        }
    }

}
