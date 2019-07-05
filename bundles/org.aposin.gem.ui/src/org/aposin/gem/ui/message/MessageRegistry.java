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
package org.aposin.gem.ui.message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.nls.Translation;

/**
 * Message registry for dynamic translation of labels.
 */
@Creatable
public final class MessageRegistry {

    private Messages messages;
    private final Map<Consumer<String>, Function<Messages, String>> translations = new HashMap<>();

    @Inject
    public void applyMessages(@Translation Messages messages) {
        this.messages = messages;
        for (final Map.Entry<Consumer<String>, Function<Messages, String>> e : translations
                .entrySet()) {
            translate(e.getKey(), e.getValue());
        }
    }

    public Messages getMessages() {
        return messages;
    }

    private final void translate(final Consumer<String> c,
            final Function<Messages, String> translateFunction) {
        if (messages != null) {
            c.accept(translateFunction.apply(messages));
        }
    }

    public void register(final Consumer<String> c,
            final Function<Messages, String> translateFunction) {
        translate(c, translateFunction);
        translations.put(c, translateFunction);
    }

}
