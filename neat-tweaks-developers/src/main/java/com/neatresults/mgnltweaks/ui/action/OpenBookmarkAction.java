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
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.action.OpenBookmarkAction.Definition;

/**
 * Action to open a page definition.
 */
public class OpenBookmarkAction extends AbstractAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(OpenBookmarkAction.class);
    private EventBus eventBus;

    private Definition definition;

    /**
     * Action to move focus to some other location.
     */
    @Inject
    public OpenBookmarkAction(Definition definition, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition);
        this.definition = definition;
        this.eventBus = eventBus;
    }


    @Override
    public void execute() throws ActionExecutionException {
        String path = StringUtils.defaultIfEmpty(definition.getPath(), "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps/browser/actionbar/sections/folders/groups/bookmarksActions/items");
        BrowserLocation location = new BrowserLocation("neatconfiguration", "browser", path + ":treeview:");
        eventBus.fireEvent(new LocationChangedEvent(location));
        // open selected node
        try {
            ContentChangedEvent cce = new ContentChangedEvent(JcrItemUtil.getItemId(RepositoryConstants.CONFIG, path), true);
            eventBus.fireEvent(cce);
        } catch (RepositoryException e) {
            log.error("Ooops, failed to retrieve node at path {} and open it while executing open bookmark action with {}", path, e.getMessage(), e);
        }
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
