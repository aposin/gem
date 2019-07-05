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
package org.aposin.gem.core.impl.internal.workflow.command;

import java.text.MessageFormat;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.aposin.gem.core.api.workflow.ICommand;
import org.aposin.gem.core.api.workflow.ICommand.IResult;

/**
 * Utility class to build a result.
 * </br>
 * The result builder can be re-used.
 */
public class ResultBuilder {

    private static final String ERROR_MESSAGE_FORMAT = "Command failed: {0}";
    private static final Supplier<String> DEFAULT_MESSAGE_SUPPLIER = () -> "Unknown error";
    private static final BooleanSupplier DEFAULT_BOOLEAN_SUPPLIER = () -> false;
    private static final BooleanSupplier ALWAYS_FAIL_SUPPLIER = () -> true;

    private final ICommand cmd;
    private Supplier<String> errorMessageSupplier = null;
    private BooleanSupplier failureSupplier = null;
    private Throwable exception;

    public static final ResultBuilder fromOther(final ICommand other, final ResultBuilder builder) {
        final ResultBuilder otherBuilder = new ResultBuilder(other);
        otherBuilder.failureSupplier = builder.failureSupplier;
        otherBuilder.errorMessageSupplier = builder.errorMessageSupplier;
        otherBuilder.exception = builder.exception;
        return otherBuilder;
    }

    public static final ResultBuilder fromResult(final ICommand cmd, final IResult result) {
        return new ResultBuilder(cmd)//
                .withFailure(result::isFailed) //
                .withErrorMessage(result::getErrorMessage)//
                .withException(result.getException());
    }

    /**
     * Result builder for the command.
     * 
     * @param cmd command.
     */
    public ResultBuilder(final ICommand cmd) {
        this.cmd = cmd;
    }

    /**
     * Adds an error message supplier.
     * </br>
     * Default message is an unknown error.
     * 
     * @param errorMessageSupplier error message supplier.
     * @return this builder.
     */
    public ResultBuilder withErrorMessage(final Supplier<String> errorMessageSupplier) {
        this.errorMessageSupplier = errorMessageSupplier;
        return this;
    }

    /**
     * Sets a conditional failure.
     * </br>
     * Default is returning {@code false} (no failure).
     * 
     * @param failureSupplier failure supplier.
     * @return this builder.
     */
    public ResultBuilder withFailure(final BooleanSupplier failureSupplier) {
        this.failureSupplier = failureSupplier;
        return this;
    }

    /**
     * Wraps an exception.
     * </br>
     * A non-null exception creates a failure if {@link #withFailure(BooleanSupplier)}
     * was not called.
     * 
     * @param exception exception.
     * @return
     */
    public ResultBuilder withException(final Throwable exception) {
        this.exception = exception;
        return this;
    }

    /**
     * Builds the error message supplier.
     * 
     * @return error message supplier.
     */
    private Supplier<String> buildErrorMsgSupplier() {
        if (errorMessageSupplier != null) {
            return errorMessageSupplier;
        }
        if (exception != null) {
            return () -> MessageFormat.format(ERROR_MESSAGE_FORMAT,
                    exception.getLocalizedMessage());
        }

        return DEFAULT_MESSAGE_SUPPLIER;
    }

    private BooleanSupplier buildFailureSupplier() {
        if (failureSupplier != null) {
            return failureSupplier;
        }
        if (exception != null) {
            return ALWAYS_FAIL_SUPPLIER;
        }

        return DEFAULT_BOOLEAN_SUPPLIER;
    }


    /**
     * Builds the result.
     * 
     * @return result.
     */
    public IResult build() {
        return new Result(cmd, buildFailureSupplier(), buildErrorMsgSupplier(), exception);
    }

    /**
     * Builds a simple failed result.
     * 
     * @param cmd command
     * @param errorMessage error message.
     * @return failed result.
     */
    public static final IResult buildFailed(final ICommand cmd, final String errorMessage) {
        return new ResultBuilder(cmd)//
                .withFailure(ALWAYS_FAIL_SUPPLIER)// always failed
                .withErrorMessage(() -> MessageFormat.format(ERROR_MESSAGE_FORMAT, errorMessage))
                .build();
    }

    /**
     * Builds a simple failed result.
     * 
     * @param cmd command
     * @param exception exception
     * @return failed result.
     */
    public static final IResult buildFailed(final ICommand cmd, final Exception exception) {
        return new ResultBuilder(cmd)//
                .withFailure(ALWAYS_FAIL_SUPPLIER)// always failed
                .withException(exception)// exception+message
                .build();
    }

    /**
     * Implementation of the {@link IResult}.
     */
    private static final class Result implements IResult {

        private final ICommand cmd;
        private final Supplier<String> errorMessageSupplier;
        private final BooleanSupplier failureSupplier;
        private final Throwable exception;

        private Result(final ICommand cmd, final BooleanSupplier failureSupplier,
                final Supplier<String> errorMessageSupplier, final Throwable exception) {
            this.cmd = cmd;
            this.errorMessageSupplier = errorMessageSupplier;
            this.failureSupplier = failureSupplier;
            this.exception = exception;
        }

        @Override
        public ICommand getCommand() {
            return cmd;
        }

        @Override
        public boolean isFailed() {
            return failureSupplier.getAsBoolean();
        }

        @Override
        public String getErrorMessage() {
            return isFailed() ? errorMessageSupplier.get() : null;
        }

        @Override
        public Throwable getException() {
            return isFailed() ? exception : null;
        }

    }

}
