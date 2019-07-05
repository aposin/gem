/**
 * Copyright 2020 Association for the promotion of open-source insurance software and for the establishment of open interface standards in the insurance industry (Verein zur Förderung quelloffener Versicherungssoftware und Etablierung offener Schnittstellenstandards in der Versicherungsbranche)
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
package org.aposin.gem.logging.utils;

import org.slf4j.Logger;

/**
 * Logging levels for the GEM application.
 */
public enum LogLevel {
	OFF(ch.qos.logback.classic.Level.OFF), //
	ERROR(ch.qos.logback.classic.Level.ERROR), //
	WARNING(ch.qos.logback.classic.Level.WARN), //
	INFO(ch.qos.logback.classic.Level.INFO), //
	ALL(ch.qos.logback.classic.Level.ALL);
	
	/**
	 * Log-level for Logback.
	 */
	/*package*/ final ch.qos.logback.classic.Level logbackLevel;
	
	private LogLevel(ch.qos.logback.classic.Level logbackLevel) {
		this.logbackLevel = logbackLevel;
	}
	
	public void log(final Logger logger, final String msg) {
		switch(this) {
			case OFF:
				// no logging if OFF
				break;
			case ERROR:
				logger.error(msg);
				break;
			case WARNING:
				logger.warn(msg);
				break;
			case INFO:
				logger.info(msg);
				break;
			case ALL:
				logger.trace(msg);
				break;
			default:
				throw new IllegalStateException("Not implemented log level " + this);
		}
	}
}
