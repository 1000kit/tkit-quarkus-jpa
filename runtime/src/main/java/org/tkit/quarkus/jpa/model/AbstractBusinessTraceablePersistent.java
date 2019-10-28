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
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Abstract base class for entities with businessId primary key. Entity class which is extending this abstract class must define
 * SequenceGenerator. As guid is no more primary key you must create index for it.
 * 
 * <pre>
 * {@code
 *  {@literal @}Table(name = "TABLE_NAME", indexes = {@literal @}Index(name = "TABLE_NAME_GUID_IDX", columnList = "GUID", unique = true))
 *  {@literal @}SequenceGenerator(name = "GEN_TABLE_NAME", sequenceName = "SEQ_TABLE_NAME_BID", allocationSize = 1, initialValue = 1)
 * }
 * </pre>

 */
@MappedSuperclass
@EntityListeners(TraceableListener.class)
public abstract class AbstractBusinessTraceablePersistent extends AbstractPersistent implements TraceablePersistent {

    /**
     * The UID for this class.
     */
    private static final long serialVersionUID = 2102461206948885441L;

    @Id
    @Column(name = "BID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GEN_BUSINESS_ID")
    private Long businessId;

    /**
     * String ID of entity
     */
    @Column(name = "GUID")
    private String guid = UUID.randomUUID().toString();

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


    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getGuid() {
        return guid;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String getCreationUser() {
        return creationUser;
    }

    @Override
    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    @Override
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public String getModificationUser() {
        return modificationUser;
    }

    @Override
    public void setModificationUser(String modificationUser) {
        this.modificationUser = modificationUser;
    }

    @Override
    public boolean isControlTraceabilityManual() {
        return controlTraceabilityManual;
    }

    @Override
    public void setControlTraceabilityManual(boolean controlTraceabilityManual) {
        this.controlTraceabilityManual = controlTraceabilityManual;
    }

    /**
     * {@inheritDoc }
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractBusinessTraceablePersistent other = (AbstractBusinessTraceablePersistent) obj;
        Object guid = getGuid();
        Object otherGuid = other.getGuid();

        if (guid == null) {
            if (otherGuid != null) {
                return false;
            } else {
                return super.equals(obj);
            }
        } else if (!guid.equals(otherGuid)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc }
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getGuid());
        return result;
    }

}
