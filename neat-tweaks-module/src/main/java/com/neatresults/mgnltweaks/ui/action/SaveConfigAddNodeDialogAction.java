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
public class SaveConfigAddNodeDialogAction extends SaveConfigDialogAction {

    private static final Logger log = LoggerFactory.getLogger(SaveConfigAddNodeDialogAction.class);
    private final EventBus eventBus;
    private SubAppContext subAppContext;
    private EventBus adminEventBus;

    @Inject
    public SaveConfigAddNodeDialogAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback, final @Named(AdmincentralEventBus.NAME) EventBus adminEventBus, final @Named(SubAppEventBus.NAME) EventBus eventBus, SubAppContext subAppContext) {
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
                    String nodePath = ((String) nodeAdapter.getItemProperty("path").getValue()).trim();
                    String nodeType = NodeTypes.ContentNode.NAME;
                    if (nodeAdapter.getItemProperty("nodeType") != null) {
                        nodeType = ((String) nodeAdapter.getItemProperty("nodeType").getValue()).trim();
                    }
                    String parentNodeType = NodeTypes.Content.NAME;
                    if (nodeAdapter.getItemProperty("parentNodeType") != null) {
                        parentNodeType = ((String) nodeAdapter.getItemProperty("parentNodeType").getValue()).trim();
                    }
                    Node node = nodeAdapter.getJcrItem();
                    String propertyName = null;
                    if (nodePath.contains("@")) {
                        propertyName = StringUtils.substringAfter(nodePath, "@").trim();
                        if (StringUtils.isEmpty(propertyName)) {
                            propertyName = null;
                        }
                        nodePath = StringUtils.substringBefore(nodePath, "@");
                    }
                    String nodeName = nodePath;
                    if (nodePath.contains("/")) {
                        nodeName = StringUtils.substringAfterLast(nodePath, "/");
                        String parentPath = StringUtils.substringBeforeLast(nodePath, "/");
                        for (String parentName : parentPath.split("/")) {
                            node = JcrUtils.getOrAddNode(node, parentName, parentNodeType);
                        }
                    }
                    node = node.addNode(nodeName, nodeType);
                    if (propertyName != null) {
                        String value = "";
                        if (nodeAdapter.getItemProperty("value") != null) {
                            value = ((String) nodeAdapter.getItemProperty("value").getValue());
                        }
                        node.setProperty(propertyName, value);
                    }
                    node.getSession().save();
                    Location location = subAppContext.getLocation();
                    String param = location.getParameter();
                    param = node.getPath() + (propertyName != null ? ("@" + propertyName) : "") + ":" + StringUtils.substringAfter(param, ":");
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

    /**
     * Definition for above action.
     */
    public static class Definition extends SaveConfigDialogActionDefinition {

        @Override
        public void setImplementationClass(Class<? extends Action> implementationClass) {
            super.setImplementationClass(SaveConfigAddNodeDialogAction.class);
        }

    }
}
