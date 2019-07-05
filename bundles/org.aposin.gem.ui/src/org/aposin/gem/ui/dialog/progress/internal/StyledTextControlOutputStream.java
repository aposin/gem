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
package org.aposin.gem.ui.dialog.progress.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

/**
 * {@link OutputStream} implementation which updates an {@link StyledText}
 * by appending new characters.
 */
public class StyledTextControlOutputStream extends OutputStream {

    private static final byte CR = '\r';
    private static final byte LF = '\n';

    private final StyledText text;
    private final StringBuffer buffer;
    private final Color foregroundColor;

    private boolean wasLastCr = false;

    public StyledTextControlOutputStream(final StyledText text, final Color foregroundColor) {
        this.text = text;
        this.foregroundColor = foregroundColor;
        this.buffer = new StringBuffer(100);
    }

    public StyledTextControlOutputStream(final StyledText text) {
        this(text, text.getForeground());
    }


    @Override
    public void write(int b) throws IOException {
        if (b == LF) {
            // takes into account \r\n and only \n: append & flush afterwards
            buffer.appendCodePoint(b);
            flush();
        } else if (wasLastCr) {
            // takes into account only \r -> flush & append afterwards
            flush();
            buffer.appendCodePoint(b);
        } else {
            // otherwise, just add
            buffer.appendCodePoint(b);
        }
        wasLastCr = b == CR;
    }

    @Override
    public void flush() {
        if (!text.isDisposed()) {
            text.getDisplay().syncExec(this::printBuffer);
        }
        buffer.delete(0, buffer.length());
    }

    @Override
    public void close() {
        flush();
    }

    private void printBuffer() {
        if (!text.isDisposed()) {
            final StyleRange range = new StyleRange();
            range.start = text.getCharCount();
            range.length = buffer.length();
            range.foreground = foregroundColor;
            text.append(buffer.toString());
            text.setStyleRange(range);
            text.setTopIndex(text.getLineCount());
        }
    }

}
