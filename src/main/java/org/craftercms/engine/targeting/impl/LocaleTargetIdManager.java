/*
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.engine.targeting.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.craftercms.engine.util.config.TargetingProperties;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Implementation of {@link org.craftercms.engine.targeting.TargetIdManager} that treats the {@link Locale} as a
 * target ID. The current target ID is one returned by {@link LocaleContextHolder#getLocale()}, and if no available
 * target IDs are specified in the site configuration, the available locales of the JVM will be used instead.
 *
 * @author avasquez
 */
public class LocaleTargetIdManager extends AbstractTargetIdManager {

    @Override
    public String getCurrentTargetId() throws IllegalStateException {
        Locale currentLocale = LocaleContextHolder.getLocale();
        if (currentLocale != null) {
            return currentLocale.toString();
        } else {
            throw new IllegalStateException("No locale associated with the current thread");
        }
    }

    @Override
    public List<String> getAvailableTargetIds() {
        String[] availableTargetIds = TargetingProperties.getAvailableTargetIds();
        if (ArrayUtils.isEmpty(availableTargetIds)) {
            List<Locale> availableLocales = LocaleUtils.availableLocaleList();
            List<String> availableLocaleStrs = new ArrayList<>(availableLocales.size());

            for (Locale locale : availableLocales) {
                availableLocaleStrs.add(locale.toString());
            }

            return availableLocaleStrs;
        } else {
            return Arrays.asList(availableTargetIds);
        }
    }

}
