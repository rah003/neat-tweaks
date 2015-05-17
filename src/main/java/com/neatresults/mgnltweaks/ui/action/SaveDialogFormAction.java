package com.neatresults.mgnltweaks.ui.action;

import static org.apache.jackrabbit.commons.JcrUtils.in;
import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.action.SaveFormActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.util.Iterator;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.action.SaveDialogFormAction.Definition;

/**
 * Action for saving Items in Forms.<br>
 * The name of the created node will be set to:<br>
 * <b>the value of the property called 'jcrName' if</b>
 * <ul>
 * <li>the node has no property called 'jcrName'</li>
 * </ul>
 * <b>the value of the property called 'name' if</b>
 * <ul>
 * <li>the node has a property called 'name' AND</li>
 * <li>the node has no property called 'jcrName' AND</li>
 * </ul>
 * <b>a default name if non of these conditions are valid</b> <br>
 * To change this behavior extend this Action and override {@link SaveDialogFormAction#setNodeName(Node, JcrNodeAdapter)}
 *
 * @see SaveFormActionDefinition
 */
public class SaveDialogFormAction extends AbstractAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(SaveDialogFormAction.class);

    protected EditorCallback callback;
    protected final EditorValidator validator;
    protected final JcrNodeAdapter item;

    public SaveDialogFormAction(Definition definition, JcrNodeAdapter item, EditorCallback callback, EditorValidator validator) {
        super(definition);
        this.callback = callback;
        this.validator = validator;
        this.item = item;
    }

    @Override
    public void execute() throws ActionExecutionException {
        // First Validate
        validator.showValidation(true);
        if (validator.isValid()) {
            try {
                final Node node = item.applyChanges();
                // Set the Node name.
                setNodeName(node, item);
                // WTF was whomever at JR dev team thinking?
                for (Property prop : in((Iterator<Property>) node.getProperties())) {
                    if (prop.getType() == PropertyType.STRING && StringUtils.isEmpty(prop.getValue().getString())) {
                        prop.remove();
                    }
                }
                Node actions = node.addNode("actions", NodeTypes.ContentNode.NAME);
                setAction(node, actions, "commit", "info.magnolia.ui.form.action.SaveFormActionDefinition");
                setAction(node, actions, "cancel", "info.magnolia.ui.form.action.CancelFormActionDefinition");

                Node tabs = node.addNode("form", NodeTypes.ContentNode.NAME).addNode("tabs", NodeTypes.ContentNode.NAME);
                for (Node n : in((Iterator<Node>) node.getNodes("tabs*"))) {
                    if (n.hasProperty("field")) {
                        String name = n.getProperty("field").getString();

                        Node tab = tabs.addNode(Path.getUniqueLabel(tabs, Path.getValidatedLabel(name)), NodeTypes.ContentNode.NAME);
                        tab.setProperty("label", StringUtils.capitalize(name));
                        tab.addNode("fields", NodeTypes.ContentNode.NAME);
                    }
                    n.remove();
                }
                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.info("Validation error(s) occurred. No save performed.");
        }
    }

    private void setAction(final Node node, Node actions, String actionName, String implClass) throws RepositoryException, PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, ItemExistsException, AccessDeniedException {
        String propName = "default" + StringUtils.capitalize(actionName);
        if (node.hasProperty(propName)) {
            Property defaultAction = node.getProperty(propName);
            if (defaultAction.getBoolean()) {
                actions.addNode(actionName, NodeTypes.ContentNode.NAME).setProperty("class", implClass);
            }
            defaultAction.remove();
        }
    }

    /**
     * Set the node Name. Node name is set to: <br>
     * the value of the property 'name' if it is present.
     */
    protected void setNodeName(Node node, JcrNodeAdapter item) throws RepositoryException {
        String propertyName = "name";
        if (node.hasProperty(propertyName) && !node.hasProperty(ModelConstants.JCR_NAME)) {
            Property property = node.getProperty(propertyName);
            String newNodeName = property.getString();
            if (!node.getName().equals(Path.getValidatedLabel(newNodeName))) {
                newNodeName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), Path.getValidatedLabel(newNodeName));
                item.setNodeName(newNodeName);
                NodeUtil.renameNode(node, newNodeName);
            }
        }

    }

    /**
     * Definition for the action.
     */
    public static class Definition extends SaveFormActionDefinition {
        public Definition() {
            setImplementationClass(SaveDialogFormAction.class);
        }
    }
}
