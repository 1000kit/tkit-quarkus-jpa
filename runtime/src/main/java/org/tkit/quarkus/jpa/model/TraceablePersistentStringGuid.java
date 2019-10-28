/*
 * Copyright 2019 1000kit.org.
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
package org.tkit.quarkus.jpa.model;

import java.util.Date;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Abstract Entity Class with a String as ID.
 */
@MappedSuperclass
@EntityListeners(TraceableListener.class)
public class TraceablePersistentStringGuid extends PersistentStringGuid implements TraceablePersistent {

    /**
     * The UID for this class.
     */
    private static final long serialVersionUID = 3699279519938221976L;

    /**
     * The creation date.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    /**
     * The creation user.
     */
    private String creationUser;
    /**
     * The modification date.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;
    /**
     * The modification user.
     */
    private String modificationUser;
    /**
     * The modification user data.
     */
    @Transient
    private boolean controlTraceabilityManual = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCreationUser() {
        return creationUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getModificationUser() {
        return modificationUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModificationUser(String modificationUser) {
        this.modificationUser = modificationUser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isControlTraceabilityManual() {
        return controlTraceabilityManual;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setControlTraceabilityManual(boolean controlTraceabilityManual) {
        this.controlTraceabilityManual = controlTraceabilityManual;
    }

}
