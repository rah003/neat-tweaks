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

import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogActionDefinition;
import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

/**
 * Dialog action that saves result of the dialog as property rather than node.
 */
public class SaveConfigPropertyDialogAction extends SaveConfigDialogAction {

    private static final Logger log = LoggerFactory.getLogger(SaveConfigPropertyDialogAction.class);
    private final EventBus eventBus;

    @Inject
    public SaveConfigPropertyDialogAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback, final @Named(SubAppEventBus.NAME) EventBus eventBus) {
        super(definition, item, validator, callback, eventBus);
        this.eventBus = eventBus;
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
                    String propName = (String) nodeAdapter.getItemProperty("name").getValue();
                    String propValue = null;
                    if (nodeAdapter.getItemProperty("value") != null) {
                        propValue = (String) nodeAdapter.getItemProperty("value").getValue();
                    } else {
                        propValue = "";
                    }
                    Node node = nodeAdapter.getJcrItem();
                    node.setProperty(propName, propValue == null ? "" : propValue);
                    node.getSession().save();
                } catch (RepositoryException e) {
                    log.error("Could not save changes to node", e);
                }

            } else if (item instanceof JcrPropertyAdapter) {
                // Saving JCR Property, update it first
                JcrPropertyAdapter propertyAdapter = (JcrPropertyAdapter) item;
                try {
                    // get parent first because once property is updated, it won't exist anymore if the name changes
                    Node parent = propertyAdapter.getJcrItem().getParent();

                    // get modifications
                    propertyAdapter.applyChanges();
                    parent.getSession().save();

                    // update workbench selection in case the property changed name
                    JcrPropertyItemId newItemId = propertyAdapter.getItemId();
                    eventBus.fireEvent(new SelectionChangedEvent(new HashSet<Object>(Arrays.asList(newItemId))));

                } catch (RepositoryException e) {
                    log.error("Could not save changes to property", e);
                }
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.debug("Validation error(s) occurred. No save performed.");
        }
    }

    /**
     * Definition for above action.
     */
    public static class Definition extends SaveConfigDialogActionDefinition {

        @Override
        public void setImplementationClass(Class<? extends Action> implementationClass) {
            super.setImplementationClass(SaveConfigPropertyDialogAction.class);
        }

    }
}
