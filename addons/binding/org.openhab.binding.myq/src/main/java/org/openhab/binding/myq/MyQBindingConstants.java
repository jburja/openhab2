/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.myq;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MyQBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jeff Burja - Initial contribution
 */
public class MyQBindingConstants {

    public static final String BINDING_ID = "myq";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CLIENT = new ThingTypeUID(BINDING_ID, "myqClient");

    // List of all Channel ids
    public final static String CHANNEL_DOOR_STATUS = "status";
    public final static String CHANNEL_DOOR_UPDATE = "door";

}
