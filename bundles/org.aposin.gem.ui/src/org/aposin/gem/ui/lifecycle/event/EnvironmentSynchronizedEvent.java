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
package org.aposin.gem.ui.lifecycle.event;

import org.aposin.gem.core.api.model.IEnvironment;

/**
 * Event triggered when an environment is synchronized.
 */
public class EnvironmentSynchronizedEvent {

	/**
	 * Topic for the event.
	 */
	public static final String TOPIC = "org/aposin/gem/core/ui/event/ENVIRONMENT_SYNCHRONIZED";

	/**
	 * Synchronized environment.
	 */
	public final IEnvironment synchronizedEnvironment;

	/**
	 * Constructor.
	 * 
	 * @param synchronizedEnvironment
	 */
	public EnvironmentSynchronizedEvent(final IEnvironment synchronizedEnvironment) {
		this.synchronizedEnvironment = synchronizedEnvironment;
	}

}
