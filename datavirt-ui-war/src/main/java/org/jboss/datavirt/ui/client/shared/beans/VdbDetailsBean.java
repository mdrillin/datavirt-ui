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
import java.util.Collection;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Models the full details of a VDB
 *
 * @author mdrillin@redhat.com
 */
@Portable
@Bindable
public class VdbDetailsBean extends VdbSummaryBean implements Serializable {

    private static final long serialVersionUID = VdbDetailsBean.class.hashCode();

    private String updatedBy;
    private String version;
    private Collection<VdbModelBean> models = new ArrayList<VdbModelBean>();

    /**
     * Constructor.
     */
    public VdbDetailsBean() {
    }

    /**
     * @return the updatedBy
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * @param updatedBy the updatedBy to set
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * @return the vdb models
     */
    public Collection<VdbModelBean> getModels() {
        return models;
    }

    /**
     * Sets the classified by.
     * @param classifiedBy
     */
    public void setModels(Collection<VdbModelBean> models) {
        this.models = models;
    }

    /**
     * @param classification
     */
    public void addModel(VdbModelBean vdbModel) {
    	models.add(vdbModel);
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
