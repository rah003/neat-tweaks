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
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.action.SaveAndRefreshGenericCommitDialogAction.Definition;
import com.vaadin.data.Item;

/**
 * Save dialog action that will force refresh and update of the parent node that was being updated ... useful to force refresh of the page editor when doing something in pages app.
 */
public class SaveAndRefreshGenericCommitDialogAction extends SaveDialogAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(SaveAndRefreshGenericCommitDialogAction.class);
    private EventBus eventBus;
    private TemplatingFunctions templatingFunctions;
    private UiContext uiContext;

    public SaveAndRefreshGenericCommitDialogAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback, @Named(SubAppEventBus.NAME) EventBus eventBus, TemplatingFunctions templatingFunctions, UiContext uiContext) {
        super(definition, item, validator, callback);
        // this sucks big time? whole class just for one bloody (could be static) util method
        this.templatingFunctions = templatingFunctions;
        this.eventBus = eventBus;
        this.uiContext = uiContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        try {
            Node node = (Node) ((JcrItemAdapter) item).getJcrItem();
            // need to mark page as modified manually? Why? I'd love to know too.
            NodeTypes.LastModified.update(node);
            node.getSession().save();

            JcrItemId itemIdOfChangedItem = JcrItemUtil.getItemId(templatingFunctions.page(node));
            eventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem));
            uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "It would seem you decided to change type of the component! In case you realize that being bad idea, you can always change it back.");
        } catch (RepositoryException e) {
            log.error("It would seem we were not able to obtain id of parent item of node {}", ((JcrItemAdapter) item).getJcrItem());
        }
    }

    /**
     * Definition for the above.
     */
    public static class Definition extends SaveDialogActionDefinition {
        public Definition() {
            setImplementationClass(com.neatresults.mgnltweaks.ui.action.SaveAndRefreshGenericCommitDialogAction.class);
        }

    }
}
