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
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogActionDefinition;
import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Dialog action that saves result of the dialog as property rather than node.
 */
public class SaveConfigAddRestClientDialogAction extends SaveConfigDialogAction {

    private static final Logger log = LoggerFactory.getLogger(SaveConfigAddRestClientDialogAction.class);
    private final EventBus eventBus;
    private SubAppContext subAppContext;
    private EventBus adminEventBus;

    @Inject
    public SaveConfigAddRestClientDialogAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback, final @Named(AdmincentralEventBus.NAME) EventBus adminEventBus, final @Named(SubAppEventBus.NAME) EventBus eventBus, SubAppContext subAppContext) {
        super(definition, item, validator, callback, eventBus);
        this.eventBus = eventBus;
        this.adminEventBus = adminEventBus;
        this.subAppContext = subAppContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {

            // we support only JCR item adapters
            if (!(item instanceof JcrItemAdapter)) {
                return;
            }

            // don't save if no value changes occurred on adapter
            if (!((JcrItemAdapter) item).hasChangedProperties()) {
                return;
            }

            if (item instanceof AbstractJcrNodeAdapter) {
                // Saving JCR Node, getting updated node first
                AbstractJcrNodeAdapter nodeAdapter = (AbstractJcrNodeAdapter) item;
                try {
                    String nodeName = ((String) nodeAdapter.getItemProperty("clientName").getValue()).trim();
                    Node node = nodeAdapter.getJcrItem();
                    String propertyName = null;
                    if ("modules".equals(node.getParent().getName())) {
                        node = JcrUtils.getOrAddNode(node, "rest-client", NodeTypes.Content.NAME);
                    }

                    node = JcrUtils.getOrAddNode(node, nodeName, NodeTypes.ContentNode.NAME);
                    setProperty(node, "baseUrl", nodeAdapter);
                    setProperty(node, "class", nodeAdapter);
                    setProperty(node, "clientFactoryClass", nodeAdapter);

                    node.getSession().save();
                    Location location = subAppContext.getLocation();
                    String param = location.getParameter();
                    param = node.getPath() + "/baseUrl";
                    location = new DefaultLocation(location.getAppType(), location.getAppName(), location.getSubAppId(), param);
                    adminEventBus.fireEvent(new LocationChangedEvent(location));
                } catch (RepositoryException e) {
                    log.error("Could not save changes to node", e);
                }
                callback.onSuccess(getDefinition().getName());
            } else if (item instanceof JcrPropertyAdapter) {
                super.execute();
            }
        } else {
            log.debug("Validation error(s) occurred. No save performed.");
        }
    }

    private void setProperty(Node node, String propertyName, AbstractJcrNodeAdapter nodeAdapter) throws RepositoryException {
        node.setProperty(propertyName, StringUtils.trimToEmpty(((String) nodeAdapter.getItemProperty(propertyName).getValue())));
    }

    /**
     * Definition for above action.
     */
    public static class Definition extends SaveConfigDialogActionDefinition {

        @Override
        public void setImplementationClass(Class<? extends Action> implementationClass) {
            super.setImplementationClass(SaveConfigAddRestClientDialogAction.class);
        }

    }
}
