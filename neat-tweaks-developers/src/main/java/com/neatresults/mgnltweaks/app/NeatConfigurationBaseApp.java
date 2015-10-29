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
package com.neatresults.mgnltweaks.app;

import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.framework.app.BaseApp;

import javax.inject.Inject;

/**
 * Neat configuration app base class.
 */
public class NeatConfigurationBaseApp extends BaseApp {

    @Inject
    public NeatConfigurationBaseApp(AppContext appContext, AppView view) {
        super(appContext, view);
    }

    @Override
    public void start(Location location) {
        super.start(location);
        // workaround to get both subapps open
        getAppContext().openSubApp(new DefaultLocation(Location.LOCATION_TYPE_APP, "neatconfiguration", "status", ""));
        getAppContext().openSubApp(new DefaultLocation(Location.LOCATION_TYPE_APP, "neatconfiguration", "browser", ""));
    }

}
