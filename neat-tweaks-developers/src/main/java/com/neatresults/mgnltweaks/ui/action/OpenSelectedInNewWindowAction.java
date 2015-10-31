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
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.NeatTweaks4DevelopersModule;
import com.neatresults.mgnltweaks.ui.action.OpenSelectedInNewWindowAction.Definition;
import com.neatresults.mgnltweaks.ui.contentapp.browser.RerootBrowserLocation;

/**
 * Action to open selected node in new subapp.
 */
public class OpenSelectedInNewWindowAction extends AbstractAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(OpenSelectedInNewWindowAction.class);
    private EventBus eventBus;

    private JcrItemAdapter item;
    private NeatTweaks4DevelopersModule module;

    /**
     * Action to move focus to some other location.
     */
    @Inject
    public OpenSelectedInNewWindowAction(Definition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, NeatTweaks4DevelopersModule module) {
        super(definition);
        this.eventBus = eventBus;
        this.item = item;
        this.module = module;
    }

    @Override
    public void execute() throws ActionExecutionException {

        // open subapp
        Location location;
        try {
            location = new RerootBrowserLocation("neatconfiguration", "helperBrowser", item.getJcrItem().getPath(), module.isShowSubtreeOnlyInHelper());
            eventBus.fireEvent(new LocationChangedEvent(location));
            // open selected node in new subwindow
            ContentChangedEvent cce = new ContentChangedEvent(item.getItemId(), true);
            eventBus.fireEvent(cce);
        } catch (RepositoryException e) {
            log.error("Ooops, while opening selected node in subapp failed to retrieve path for item {} with {} ", item, e.getMessage(), e);
        }

    }


    /**
     * Definition for the above class.
     */
    public static class Definition extends ConfiguredActionDefinition {

        public Definition() {
            setImplementationClass(OpenSelectedInNewWindowAction.class);
        }
    }
}
