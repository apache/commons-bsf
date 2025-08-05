/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf.util.cf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.bsf.util.IndentWriter;
import org.apache.bsf.util.StringUtils;

/**
 * A {@code CodeFormatter} bean is used to format raw Java code. It indents, word-wraps, and replaces tab characters with an amount of space characters
 * equal to the size of the {@code indentationStep} property. To create and use a {@code CodeFormatter}, you simply instantiate a new
 * {@code CodeFormatter} bean, and invoke {@code formatCode(Reader source, Writer target)} with appropriate arguments.
 *
 * @version 1.0
 */
public class CodeFormatter {
    /**
     * The default maximum line length.
     */
    public static final int DEFAULT_MAX = 74;
    /**
     * The default size of the indentation step.
     */
    public static final int DEFAULT_STEP = 2;
    /**
     * The default set of delimiters.
     */
    public static final String DEFAULT_DELIM = "(+";
    /**
     * The default set of sticky delimiters.
     */
    public static final String DEFAULT_S_DELIM = ",";

    // Configurable Parameters
    private int maxLineLength = DEFAULT_MAX;
    private int indentationStep = DEFAULT_STEP;
    private String delimiters = DEFAULT_DELIM;
    private String stickyDelimiters = DEFAULT_S_DELIM;

    // Global Variables
    private int indent;
    private int hangingIndent;
    private int origIndent;
    private boolean inCPP_Comment;

    private void addTok(final StringBuilder targetBuf, final StringBuilder tokBuf, final IndentWriter out) {
        final int tokLength = tokBuf.length(), targetLength = targetBuf.length();

        if (indent + targetLength + tokLength > maxLineLength) {
            if (targetLength == 0) {
                out.println(indent, tokBuf.toString());
                indent = hangingIndent;
                targetBuf.setLength(0);

                return;
            } else {
                out.println(indent, targetBuf.toString().trim());
                indent = hangingIndent;
                targetBuf.setLength(0);
            }
        }

        targetBuf.append(tokBuf.toString());
    }

    /**
     * Formats the code read from {@code source}, and writes the formatted code to {@code target}.
     *
     * @param source where to read the unformatted code from.
     * @param target where to write the formatted code to.
     */
    public void formatCode(final Reader source, final Writer target) {
        String line;
        final BufferedReader in = new BufferedReader(source);
        final IndentWriter out = new IndentWriter(new BufferedWriter(target), true);

        try {
            origIndent = 0;
            inCPP_Comment = false;

            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.length() > 0) {
                    indent = origIndent;
                    hangingIndent = indent + indentationStep;
                    printLine(line, out);
                } else {
                    out.println();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the set of delimiters.
     *
     * @return the set of delimiters.
     * @see #setDelimiters
     */
    public String getDelimiters() {
        return delimiters;
    }

    /**
     * Gets the size of the indentation step.
     *
     * @return the size of the indentation step.
     * @see #setIndentationStep
     */
    public int getIndentationStep() {
        return indentationStep;
    }

    /**
     * Gets the maximum line length.
     *
     * @return the maximum line length.
     * @see #setMaxLineLength
     */
    public int getMaxLineLength() {
        return maxLineLength;
    }

    /**
     * Gets the set of sticky delimiters.
     *
     * @return the set of sticky delimiters.
     * @see #setStickyDelimiters
     */
    public String getStickyDelimiters() {
        return stickyDelimiters;
    }

    private void printLine(final String line, final IndentWriter out) {
        final char[] source = line.toCharArray();
        char ch;
        char quoteChar = ' ';
        boolean inEscapeSequence = false;
        boolean inString = false;
        final StringBuilder tokBuf = new StringBuilder(), targetBuf = new StringBuilder(hangingIndent + line.length());

        for (int i = 0; i < source.length; i++) {
            ch = source[i];

            if (inEscapeSequence) {
                tokBuf.append(ch);
                inEscapeSequence = false;
            } else {
                if (inString) {
                    switch (ch) {
                    case '\\':
                        tokBuf.append('\\');
                        inEscapeSequence = true;
                        break;
                    case '\'':
                    case '\"':
                        tokBuf.append(ch);

                        if (ch == quoteChar) {
                            addTok(targetBuf, tokBuf, out);
                            tokBuf.setLength(0);
                            inString = false;
                        }
                        break;
                    case 9: // pass thru tab characters...
                        tokBuf.append(ch);
                        break;
                    default:
                        if (ch > 31) {
                            tokBuf.append(ch);
                        }
                        break;
                    }
                } else // !inString
                {
                    if (inCPP_Comment) {
                        tokBuf.append(ch);

                        if (ch == '/' && i > 0 && source[i - 1] == '*') {
                            inCPP_Comment = false;
                        }
                    } else {
                        switch (ch) {
                        case '/':
                            tokBuf.append(ch);

                            if (i > 0 && source[i - 1] == '/') {
                                final String tokStr = tokBuf.append(source, i + 1, source.length - (i + 1)).toString();

                                out.println(indent, targetBuf.append(tokStr).toString());

                                return;
                            }
                            break;
                        case '*':
                            tokBuf.append(ch);

                            if (i > 0 && source[i - 1] == '/') {
                                inCPP_Comment = true;
                            }
                            break;
                        case '\'':
                        case '\"':
                            addTok(targetBuf, tokBuf, out);
                            tokBuf.setLength(0);
                            tokBuf.append(ch);
                            quoteChar = ch;
                            inString = true;
                            break;
                        case 9: // replace tab characters...
                            tokBuf.append(StringUtils.getChars(indentationStep, ' '));
                            break;
                        case '{':
                            tokBuf.append(ch);
                            origIndent += indentationStep;
                            break;
                        case '}':
                            tokBuf.append(ch);
                            origIndent -= indentationStep;

                            if (i == 0) {
                                indent = origIndent;
                            }
                            break;
                        default:
                            if (ch > 31) {
                                if (delimiters.indexOf(ch) != -1) {
                                    addTok(targetBuf, tokBuf, out);
                                    tokBuf.setLength(0);
                                    tokBuf.append(ch);
                                } else if (stickyDelimiters.indexOf(ch) != -1) {
                                    tokBuf.append(ch);
                                    addTok(targetBuf, tokBuf, out);
                                    tokBuf.setLength(0);
                                } else {
                                    tokBuf.append(ch);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (tokBuf.length() > 0) {
            addTok(targetBuf, tokBuf, out);
        }

        final String lastLine = targetBuf.toString().trim();

        if (lastLine.length() > 0) {
            out.println(indent, lastLine);
        }
    }

    /**
     * Sets the set of delimiters; default set is {@code "(+"}.
     * <p>
     * Each character represents one delimiter. If a line is ready to be word-wrapped and a delimiter is encountered, the delimiter will appear as the <em>first
     * character on the following line</em>. A quotation mark, {@code "} or {@code '}, opening a string is always a delimiter, whether you specify it
     * or not.
     *
     * @param newDelimiters the new set of delimiters.
     * @see #getDelimiters
     */
    public void setDelimiters(final String newDelimiters) {
        delimiters = newDelimiters;
    }

    /**
     * Sets the size of the indentation step; default size is {@code 2}.
     * <p>
     * This is the number of spaces that lines will be indented (when appropriate).
     *
     * @param newIndentationStep the new size of the indentation step.
     * @see #getIndentationStep
     */
    public void setIndentationStep(final int newIndentationStep) {
        indentationStep = (newIndentationStep < 0 ? 0 : newIndentationStep);
    }

    /**
     * Sets the (desired) maximum line length; default length is {@code 74}.
     * <p>
     * If a token is longer than the requested maximum line length, then the line containing that token will obviously be longer than the desired maximum.
     *
     * @param newMaxLineLength the new maximum line length.
     * @see #getMaxLineLength
     */
    public void setMaxLineLength(final int newMaxLineLength) {
        maxLineLength = (newMaxLineLength < 0 ? 0 : newMaxLineLength);
    }

    /**
     * Sets the set of sticky delimiters; default set is {@code ","}.
     * <p>
     * Each character represents one sticky delimiter. If a line is ready to be word-wrapped and a sticky delimiter is encountered, the sticky delimiter will
     * appear as the <em>last character on the current line</em>. A quotation mark, {@code "} or {@code '}, closing a string is always a sticky
     * delimiter, whether you specify it or not.
     *
     * @param newStickyDelimiters the new set of sticky delimiters.
     * @see #getStickyDelimiters
     */
    public void setStickyDelimiters(final String newStickyDelimiters) {
        stickyDelimiters = newStickyDelimiters;
    }
}
