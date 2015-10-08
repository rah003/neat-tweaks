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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.action.AddBookmarkAction.Definition;

/**
 * Action to open a page definition.
 */
public class AddBookmarkAction extends AbstractAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(AddBookmarkAction.class);
    private SubAppContext subAppContext;
    private EventBus eventBus;

    private final LocationController locationController;
    private Definition definition;
    private JcrItemAdapter item;
    private UiContext uiContext;
    private AppController appController;

    /**
     * Action to move focus to some other location.
     */
    @Inject
    public AddBookmarkAction(Definition definition, JcrItemAdapter item, SubAppContext subAppContext, @Named(AdmincentralEventBus.NAME) EventBus eventBus, LocationController locationController, UiContext uiContext, AppController appController) {
        super(definition);
        this.definition = definition;
        this.locationController = locationController;
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.item = item;
        this.uiContext = uiContext;
        this.appController = appController;
    }


    @Override
    public void execute() throws ActionExecutionException {
        try {
            final String path = item.getJcrItem().getPath();

            Session session = MgnlContext.getJCRSession("config");
            Node bar = session.getNode("/modules/neat-tweaks-developers/apps/neatconfiguration/subApps/browser/actionbar/sections/folders/groups/bookmarksActions/items");
            String name = "bkmk" + StringUtils.capitalize(item.getJcrItem().getName());
            bar.addNode(name, NodeTypes.ContentNode.NAME);
            Node actions = session.getNode("/modules/neat-tweaks-developers/apps/neatconfiguration/subApps/browser/actions");
            Node action = actions.addNode(name, NodeTypes.ContentNode.NAME);
            action.setProperty("extends", "../manageBookmarks");
            action.setProperty("icon", "icon-favorites");
            action.setProperty("path", path);
            action.setProperty("label", item.getJcrItem().getName());
            session.save();
            uiContext.openConfirmation(MessageStyleTypeEnum.INFO, "Wanna wait?", "To add a bookmark, one need to refresh the app, to refresh the app, one must kill it. To make this harder, observation kicks in only every 4 seconds so you got to wait for the refresh.",
                    "OK, I'll take a nap", "no way", false, new ConfirmationCallback() {

                        @Override
                        public void onCancel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onSuccess() {
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException e) {
                                // meh
                            }

                            // subAppContext.getAppContext().closeSubApp(subAppContext.getInstanceId());
                            // subAppContext.getAppContext().openSubApp(location);
                            appController.stopCurrentApp();
                            BrowserLocation location = new BrowserLocation("neatconfiguration", "helperBrowser", path + ":treeview:");
                            eventBus.fireEvent(new LocationChangedEvent(location));
                        }

                    });
        } catch (RepositoryException e) {
            log.error("Ooops, failed to add bookmark for {} with {}.", item, e.getMessage(), e);
        }

    }


    /**
     * Definition for the above class.
     */
    public static class Definition extends ConfiguredActionDefinition {

        private String path;

        public Definition() {
            setImplementationClass(AddBookmarkAction.class);
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
