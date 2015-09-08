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
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.action.SaveAndRefreshPageAction.Definition;

/**
 * Dialog action that saves result of the dialog as property rather than node.
 */
public class SaveAndRefreshPageAction extends SaveDialogAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(SaveAndRefreshPageAction.class);
    private final EventBus eventBus;
    private SubAppContext subAppContext;
    private EventBus adminEventBus;
    private JcrItemAdapter item;
    private TemplatingFunctions templatingFunctions;
    private UiContext uiContext;


    @Inject
    public SaveAndRefreshPageAction(Definition definition, JcrItemAdapter item, EditorValidator validator, EditorCallback callback, final @Named(AdmincentralEventBus.NAME) EventBus adminEventBus, final @Named(SubAppEventBus.NAME) EventBus eventBus, SubAppContext subAppContext,
            TemplatingFunctions templatingFunctions, UiContext uiContext) {
        super(definition, item, validator, callback);
        this.item = item;
        this.eventBus = eventBus;
        this.adminEventBus = adminEventBus;
        this.subAppContext = subAppContext;
        this.templatingFunctions = templatingFunctions;
        this.uiContext = uiContext;

    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            try {
                Node node = (Node) item.getJcrItem();
                // need to mark page as modified manually? Why? I'd love to know too.
                NodeTypes.LastModified.update(node);
                node.getSession().save();

                JcrItemId itemIdOfChangedItem = JcrItemUtil.getItemId(templatingFunctions.page(node));
                eventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem));
                uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "In case you didn't manage to set component type right, you can always try again ... and again");
            } catch (RepositoryException e) {
                log.error("It would seem we were not able to obtain id of parent item of node {}", item.getJcrItem());
            }

        } else {
            log.debug("Validation error(s) occurred. No save performed.");
        }
    }

    /**
     * Definition for above action.
     */
    public static class Definition extends SaveDialogActionDefinition {
        public Definition() {
            setImplementationClass(SaveAndRefreshPageAction.class);
        }

    }
}
