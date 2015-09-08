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
