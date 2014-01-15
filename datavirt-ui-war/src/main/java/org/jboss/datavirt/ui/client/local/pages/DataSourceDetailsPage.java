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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.datavirt.ui.client.local.ClientMessages;
import org.jboss.datavirt.ui.client.local.pages.datasources.DataSourcePropertiesTable;
import org.jboss.datavirt.ui.client.local.services.DataSourceRpcService;
import org.jboss.datavirt.ui.client.local.services.NotificationService;
import org.jboss.datavirt.ui.client.local.services.rpc.IRpcServiceInvocationHandler;
import org.jboss.datavirt.ui.client.shared.beans.DataSourceDetailsBean;
import org.jboss.datavirt.ui.client.shared.beans.DataSourcePropertyBean;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;

/**
 * The page shown to the user when they click on one of the Data Sources
 * displayed in the Data Source Table on the DataSources page.
 *
 * @author mdrillin@redhat.com
 */
@Templated("/org/jboss/datavirt/ui/client/local/site/datasource-details.html#page")
@Page(path="datasourcedetails")
@Dependent
public class DataSourceDetailsPage extends AbstractPage {

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
    
    protected DataSourceDetailsBean currentDataSourceDetails;

    @PageState
    private String name;

    // Breadcrumbs
    @Inject @DataField("back-to-datasources")
    TransitionAnchor<DataSourcesPage> backToDataSources;

    @Inject @DataField("datasourcedetails-pagetitle")
    protected Label pageTitle;

    @Inject @DataField("datasource-core-properties-table")
    protected DataSourcePropertiesTable dataSourceCorePropertiesTable;
    @Inject @DataField("datasource-advanced-properties-table")
    protected DataSourcePropertiesTable dataSourceAdvancedPropertiesTable;
    // Actions
//    @Inject  @DataField("btn-delete")
//    Button deleteButton;
//    @Inject
//    DeleteDataSourceDialog deleteDataSourceDialog;

    // Overview tab
//    @Inject @DataField("core-property-name") @Bound(property="name")
//    EditableInlineLabel propName;
//
//    @Inject @DataField("link-download-content")
//    Anchor downloadContentLink;
//    @Inject @DataField("link-download-metaData")
//    Anchor downloadMetaDataLink;

    @Inject @DataField("datasource-details-loading-spinner")
    protected HtmlSnippet datasourceLoading;
//    protected Element pageContent;
    //protected Element editorWrapper;

    /**
     * Constructor.
     */
    public DataSourceDetailsPage() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
//        deleteDataSourceDialog.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                //onDeleteConfirm();
//            }
//        });

    }

    /**
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
    	
        // Get the Data Source Details
    	doGetDataSourceDetails( );
    }

    /**
     * Get the Data Source details for the current Data Source name.
     */
    protected void doGetDataSourceDetails( ) {
        currentDataSourceDetails = null;
        
        dataSourceService.getDataSourceDetails(name, new IRpcServiceInvocationHandler<DataSourceDetailsBean>() {
            @Override
            public void onReturn(DataSourceDetailsBean dsDetailsBean) {
            	currentDataSourceDetails = dsDetailsBean;
            	String title = "Data Source : "+dsDetailsBean.getName();
            	pageTitle.setText(title);
            	
            	populatePropertiesTable(dsDetailsBean);
//                updateVdbModelsTable(vdbDetailsBean);
//                updateDownloadVdbLink(vdbDetailsBean);
//                updatePager(data);
//                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
//                searchInProgressMessage.setVisible(false);
                pageTitle.setText("unknown");
            }
        });       
        
    }
    
    private void populatePropertiesTable(DataSourceDetailsBean dsDetailsBean) {
    	List<DataSourcePropertyBean> propList = dsDetailsBean.getProperties();
    	
    	dataSourceCorePropertiesTable.clear();
    	dataSourceAdvancedPropertiesTable.clear();
    	
    	for(DataSourcePropertyBean defn : propList) {
    		if(defn.isCoreProperty()) {
    			dataSourceCorePropertiesTable.addRow(defn);
    		} else {
    			dataSourceAdvancedPropertiesTable.addRow(defn);
    		}
    	}
    	dataSourceCorePropertiesTable.setVisible(true);
    	dataSourceAdvancedPropertiesTable.setVisible(true);
    }
    
    /**
     * Called when the user clicks the Add Property button.
     * @param event
     */
//    @EventHandler("add-property-button")
//    protected void onAddProperty(ClickEvent event) {
//        AddDataSourceDialog dialog = addPropertyDialogFactory.get();
//        dialog.addValueChangeHandler(new ValueChangeHandler<Map.Entry<String,String>>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<Entry<String, String>> event) {
//                Entry<String, String> value = event.getValue();
//                if (value != null) {
//                    String propName = value.getKey();
//                    String propValue = value.getValue();
//                    Map<String, String> newProps = new HashMap<String,String>(artifact.getModel().getProperties());
//                    newProps.put(propName, propValue);
//                    customProperties.setValue(newProps, true);
//                }
//            }
//        });
//        dialog.show();
//    }

    /**
     * Called when the user clicks the Delete button.
     * @param event
     */
//    @EventHandler("btn-delete")
//    protected void onDeleteClick(ClickEvent event) {
////        deleteDialog.setArtifactName(artifact.getModel().getName());
////        deleteDialog.show();
//    }

    /**
     * Called when the user confirms the deletion.
     */
    protected void onDeleteConfirm() {
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("artifact-details.deleting-artifact-title"), //$NON-NLS-1$
//                i18n.format("artifact-details.deleting-artifact-msg", artifact.getModel().getName())); //$NON-NLS-1$
//        artifactService.delete(artifact.getModel(), new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("artifact-details.artifact-deleted"), //$NON-NLS-1$
//                        i18n.format("artifact-details.delete-success-msg", artifact.getModel().getName())); //$NON-NLS-1$
//                backToDataSources.click();
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("artifact-details.delete-error"), //$NON-NLS-1$
//                        error);
//            }
//        });
    }

    protected void update(DataSourceDetailsBean dataSource) {
//        this.dataSource.setModel(dataSource, InitialState.FROM_MODEL);
        String contentUrl = GWT.getModuleBaseURL() + "services/dataVirtDownload"; //$NON-NLS-1$
        contentUrl += "?name=" + dataSource.getName() + "&type=" + dataSource.getType(); //$NON-NLS-1$ //$NON-NLS-2$
        String metaDataUrl = contentUrl + "&as=meta-data"; //$NON-NLS-1$
//        this.downloadContentLink.setHref(contentUrl);
//        this.downloadContentLink.setVisible(!dataSource.isDerived());
//        this.downloadMetaDataLink.setHref(metaDataUrl);
//
//        if (dataSource.isDocument()) {
//            this.downloadContentLink.getElement().removeClassName("hidden"); //$NON-NLS-1$
//        } else {
//            this.downloadContentLink.getElement().addClassName("hidden"); //$NON-NLS-1$
//        }
//
//        deleteButton.setVisible(!dataSource.isDerived());

        datasourceLoading.getElement().addClassName("hide"); //$NON-NLS-1$
    }

    /**
     * Sends the model back up to the server (saves local changes).
     */
//    protected void pushModelToServer() {
//        String noteTitle = i18n.format("artifact-details.updating-artifact.title"); //$NON-NLS-1$
//        String noteBody = i18n.format("artifact-details.updating-artifact.message", artifact.getModel().getName()); //$NON-NLS-1$
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                noteTitle, noteBody);
//        artifactService.update(artifact.getModel(), new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                String noteTitle = i18n.format("artifact-details.updated-artifact.title"); //$NON-NLS-1$
//                String noteBody = i18n.format("artifact-details.updated-artifact.message", artifact.getModel().getName()); //$NON-NLS-1$
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        noteTitle, noteBody);
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("artifact-details.error-updating-arty"), //$NON-NLS-1$
//                        error);
//            }
//        });
//    }

}
