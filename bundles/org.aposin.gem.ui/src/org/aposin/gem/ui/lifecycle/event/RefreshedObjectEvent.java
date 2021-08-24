/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur Foerderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.ui.lifecycle.event;

import org.aposin.gem.core.api.config.IConfiguration;

/**
 * Event triggered when an object is refreshed.
 * 
 * @param <T> type of the object.
 */
public class RefreshedObjectEvent<T> {

	/**
	 * Topic sent when the {@link IConfiguration#refresh()} is called.
	 * </br>
	 * This topic should be send only with type {@link IConfiguration}.
	 */
	public static final String SESSION_CONFIG_REFRESH_TOPIC = "org/aposin/gem/core/ui/event/SESSION_CONFIG_REFRESH";
	
	/**
	 * Refreshed object.
	 */
	public final T refreshedObject;
	
	public RefreshedObjectEvent(final T refreshedObject) {
		this.refreshedObject = refreshedObject;
	}
	
}
