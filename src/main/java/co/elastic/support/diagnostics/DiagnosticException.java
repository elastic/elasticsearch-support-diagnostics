/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package co.elastic.support.diagnostics;

public class DiagnosticException extends Exception {
    public DiagnosticException() {
        super();
    }

    public DiagnosticException(String message) {
        super(message);
    }

    public DiagnosticException(String message, Throwable cause) {
        super(message, cause);
    }
}