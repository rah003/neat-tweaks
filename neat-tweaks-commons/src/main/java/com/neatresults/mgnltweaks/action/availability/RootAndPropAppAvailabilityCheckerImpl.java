/**
 *
 * Copyright 2015 by Jan Haderka <jan.haderka@neatresults.com>
 *
 * This file is part of neat-tweaks module.
 *
 * Neat-tweaks is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Neat-tweaks is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neat-tweaks.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0 <http://www.gnu.org/licenses/gpl.txt>
 *
 * Should you require distribution under alternative license in order to
 * use neat-tweaks commercially, please contact owner at the address above.
 *
 */
package com.neatresults.mgnltweaks.action.availability;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.framework.availability.AvailabilityCheckerImpl;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.List;

import javax.inject.Inject;

/**
 * Availability checker with simple flag to override all rules for non content change related actions.
 */
public class RootAndPropAppAvailabilityCheckerImpl extends AvailabilityCheckerImpl {

    @Inject
    public RootAndPropAppAvailabilityCheckerImpl(ComponentProvider componentProvider, ContentConnector contentConnector) {
        super(componentProvider, contentConnector);
    }

    @Override
    public boolean isAvailable(AvailabilityDefinition definition, List<Object> ids) {
        if (definition instanceof Definition) {
            if (((Definition) definition).isAlwaysOn()) {
                return true;
            }
        }
        return super.isAvailable(definition, ids);
    }

    /**
     * Definition for the outer class.
     */
    public static class Definition extends ConfiguredAvailabilityDefinition {
        private boolean alwaysOn = true;

        public boolean isAlwaysOn() {
            return alwaysOn;
        }

        public void setAlwaysOn(boolean alwaysOn) {
            this.alwaysOn = alwaysOn;
        }

    }
}
