/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.crafter.engine.exception;

import org.craftercms.crafter.core.exception.CrafterException;

/**
 * Thrown if an error prevents from registering a site.
 *
 * @author Alfonso Vásquez
 */
public class SiteRegistrationException extends CrafterException {

    public SiteRegistrationException() {
    }

    public SiteRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SiteRegistrationException(String message) {
        super(message);
    }

    public SiteRegistrationException(Throwable cause) {
        super(cause);
    }

}
