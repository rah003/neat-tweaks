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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.framework.action.DuplicateNodeAction;
import info.magnolia.ui.framework.action.DuplicateNodeActionDefinition;
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

/**
 * Action to duplicate component in an area in a page.
 */
public class DuplicatePageComponentAction extends DuplicateNodeAction {

    private static final Logger log = LoggerFactory.getLogger(DuplicatePageComponentAction.class);
    private EventBus eventBus;
    private TemplatingFunctions templatingFunctions;
    private UiContext uiContext;
    private JcrItemId changedId;

    @Inject
    public DuplicatePageComponentAction(DuplicateNodeActionDefinition definition, JcrItemAdapter item, @Named(SubAppEventBus.NAME) EventBus eventBus, TemplatingFunctions templatingFunctions, UiContext uiContext) {
        super(definition, item, eventBus);
        // this sucks big time? whole class just for one bloody (could be static) util method
        this.templatingFunctions = templatingFunctions;
        this.eventBus = eventBus;
        this.uiContext = uiContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        try {
            Node node = (Node) item.getJcrItem();
            NodeUtil.orderAfter((Node) JcrItemUtil.getJcrItem(changedId), node.getName());
            // need to mark page as modified manually? Why? I'd love to know too.
            NodeTypes.LastModified.update(node);
            node.getSession().save();

            JcrItemId itemIdOfChangedItem = JcrItemUtil.getItemId(templatingFunctions.page(node));
            eventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem));
            uiContext.openNotification(MessageStyleTypeEnum.INFO, true, "The item your grace requested have been duplicated! How may I serve my lord further?");
        } catch (RepositoryException e) {
            log.error("It would seem we were not able to obtain id of parent item of node {}", item.getJcrItem());
        }
    }

    @Override
    protected void setItemIdOfChangedItem(JcrItemId itemIdOfChangedItem) {
        changedId = itemIdOfChangedItem;
        super.setItemIdOfChangedItem(itemIdOfChangedItem);
    }

    /**
     * Definition for action above.
     */
    public static class Definition extends DuplicateNodeActionDefinition {

        public Definition() {
            setImplementationClass(DuplicatePageComponentAction.class);
        }
    }
}
