/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.protocol;

/**
 * Requested operation of a command to the Lutron integration protocol.
 *
 * @author Allan Tong - Initial contribution
 *
 */
public enum LutronOperation {
    EXECUTE("#"),
    QUERY("?");

    private final String operationChar;

    LutronOperation(String operationChar) {
        this.operationChar = operationChar;
    }

    @Override
    public String toString() {
        return this.operationChar;
    }
}
