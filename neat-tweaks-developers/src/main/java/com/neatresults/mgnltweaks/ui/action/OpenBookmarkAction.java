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
package com.neatresults.mgnltweaks.ui.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.neatresults.mgnltweaks.ui.action.OpenBookmarkAction.Definition;

/**
 * Action to open a page definition.
 */
public class OpenBookmarkAction extends AbstractAction<Definition> {

    private SubAppContext subAppContext;
    private EventBus eventBus;

    private final LocationController locationController;
    private Definition definition;

    /**
     * Action to move focus to some other location.
     */
    @Inject
    public OpenBookmarkAction(Definition definition, SubAppContext subAppContext, @Named(AdmincentralEventBus.NAME) EventBus eventBus, LocationController locationController) {
        super(definition);
        this.definition = definition;
        this.locationController = locationController;
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
    }


    @Override
    public void execute() throws ActionExecutionException {
        String path = StringUtils.defaultIfEmpty(definition.getPath(), "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps/browser/actionbar/sections/folders/groups/importExportActions/items");
        BrowserLocation location = new BrowserLocation("neatconfiguration", "browser", path + ":treeview:");
        eventBus.fireEvent(new LocationChangedEvent(location));
        // locationController.goTo(location);
    }


    /**
     * Definition for the above class.
     */
    public static class Definition extends ConfiguredActionDefinition {

        private String path;

        public Definition() {
            setImplementationClass(OpenBookmarkAction.class);
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
