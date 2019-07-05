/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur FÃ¶rderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aposin.gem.core.api;

import org.aposin.gem.core.api.model.IEnvironment;
import org.aposin.gem.core.api.model.IProject;

/**
 * Interface for a named object in the GEM API.
 * </br>
 * There are 3 different names for the objects:
 * <ul>
 *  <li>
 *      ID: identifies the object and might or not contain other objects attached to it.
 *      For example, an {@link IEnvironment} is always attached to a {@link IProject} and
 *      thus is identified by the project-ID plus the name of the envioronment.
 *  </li>
 *  <li>
 *      Name: identifies the object with an unique ID, but this could be shared by several
 *      other objects. For example, an {@link IEnvironment} could have a name that is
 *      the same between different projects. A use-case are {@link IProject}s based on
 *      the same repository with different imports; thus, a {@link IEnvironment} name
 *      could be the version number and shared between the two; then, the ID would
 *      contain both (see above).
 *  </li>
 *  <li>
 *      Display name: name to be used for display, and might contain spaces, special
 *      characters, etc. This is useful to show an user-friendly name for the object.
 *  </li>
 * </uL>
 */
public interface INamedObject extends Comparable<INamedObject> {

    /**
     * Gets the id of the object.
     * </br>
     * Default implementation returns {@link #getName()},
     * useful for most of the cases.
     * 
     * @return id of the object.
     */
    public default String getId() {
        return getName();
    }

    /**
     * Gets the name for the object.
     * </br>
     * The name of the object should be code-friendly
     * and folder/file-friendly (e.g., without white-space).
     * What is "friendly" depends on the object itself.
     * 
     * @return name without special characters.
     */
    public String getName();

    /**
     * Gets the display name of the object.
     * </br>
     * This display name differs from the {@link #getName()}
     * in that it is user-friendly (e.g., readable).
     * 
     * @return display name.
     */
    public String getDisplayName();

    /**
     *{@inheritDoc}
     *</br>
     * 
     * @param o other named object.
     * 
     * @return default implementation compares the {@link #getId()} method;
     *         this implementation might be overriden.
     */
    @Override
    public default int compareTo(final INamedObject o) {
        return getId().compareTo(o.getId());
    }

}
