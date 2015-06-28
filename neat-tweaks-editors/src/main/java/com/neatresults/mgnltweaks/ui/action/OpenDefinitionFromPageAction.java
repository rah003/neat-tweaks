package com.neatresults.mgnltweaks.ui.action;

import static com.neatresults.mgnltweaks.NeatUtil.templateIdToPath;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenterFactory;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.neatresults.mgnltweaks.ui.action.OpenDefinitionFromPageAction.Definition;

/**
 * Action to open a page definition.
 */
public class OpenDefinitionFromPageAction extends AbstractAction<Definition> {

    private FormDialogPresenterFactory dialogPresenterFactory;
    private AbstractElement element;
    private SubAppContext subAppContext;
    private EventBus eventBus;

    private final LocationController locationController;

    @Inject
    public OpenDefinitionFromPageAction(Definition definition, AbstractElement element, SubAppContext subAppContext, @Named(AdmincentralEventBus.NAME) EventBus eventBus, FormDialogPresenterFactory dialogPresenterFactory, LocationController locationController) {
        super(definition);
        this.locationController = locationController;
        this.element = element;
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.dialogPresenterFactory = dialogPresenterFactory;
    }


    @Override
    public void execute() throws ActionExecutionException {
        try {
            String workspace = element.getWorkspace();
            String path = element.getPath();
            Session session = MgnlContext.getJCRSession(workspace);
            if (path == null || !session.itemExists(path)) {
                path = "/";
            }
            final Node node = session.getNode(path);

            String templatePath = null;
            String templateId = node.getProperty(NodeTypes.Renderable.TEMPLATE).getString();
            if (NodeUtil.isNodeType(node, NodeTypes.Page.NAME) || NodeUtil.isNodeType(node, NodeTypes.Component.NAME)) {
                // open component definition
                templatePath = templateIdToPath(templateId);
            }
            if (templatePath == null) {
                throw new ActionExecutionException("Not able to open definition for " + node.getPath());
            }

            BrowserLocation location = new BrowserLocation("neatconfiguration", "helperBrowser", templatePath + ":treeview:");
            eventBus.fireEvent(new LocationChangedEvent(location));
            // locationController.goTo(location);

        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
    }


    /**
     * Definition for the above class.
     */
    public static class Definition extends ConfiguredActionDefinition {

        public Definition() {
            setImplementationClass(OpenDefinitionFromPageAction.class);
        }
    }
}
