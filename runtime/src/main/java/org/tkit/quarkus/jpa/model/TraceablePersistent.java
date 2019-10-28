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

/**
 * The traceable persistent interface.
 */
public interface TraceablePersistent {

    /**
     * Get the creation date.
     *
     * @return the creation date.
     */
    Date getCreationDate();

    /**
     * Set Get the creation date.
     *
     * @param creationDate the creation date.
     */
    void setCreationDate(Date creationDate);

    /**
     * Get the creation user.
     *
     * @return the creation user.
     */
    String getCreationUser();

    /**
     * Set the creation user.
     *
     * @param creationUser the creation user.
     */
    void setCreationUser(String creationUser);

    /**
     * Get the modification date.
     *
     * @return the modification date.
     */
    Date getModificationDate();

    /**
     * Set the modification date.
     *
     * @param modificationDate the modification date.
     */
    void setModificationDate(Date modificationDate);

    /**
     * Get the modification user.
     *
     * @return the modification user.
     */
    String getModificationUser();

    /**
     * Set the modification user.
     *
     * @param modificationUser the modification user.
     */
    void setModificationUser(String modificationUser);

    /**
     * Is control traceability manual flag.
     *
     * @return Is control traceability manual flag.
     */
    boolean isControlTraceabilityManual();

    /**
     * Sets control traceability manual flag.
     *
     * @param controlTraceabilityManual control traceability manual flag.
     */
    void setControlTraceabilityManual(boolean controlTraceabilityManual);

}
