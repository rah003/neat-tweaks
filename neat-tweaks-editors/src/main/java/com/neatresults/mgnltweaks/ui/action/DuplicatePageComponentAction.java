package com.neatresults.mgnltweaks.ui.action;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
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


    /**
     * Definition for action above.
     */
    public static class Definition extends DuplicateNodeActionDefinition {

        public Definition() {
            setImplementationClass(DuplicatePageComponentAction.class);
        }
    }
}
