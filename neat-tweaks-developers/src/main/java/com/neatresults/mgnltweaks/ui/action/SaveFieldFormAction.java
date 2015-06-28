package com.neatresults.mgnltweaks.ui.action;

import static org.apache.jackrabbit.commons.JcrUtils.in;
import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.action.SaveFormActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.action.SaveFieldFormAction.Definition;

/**
 * Action for saving fields.
 */
public class SaveFieldFormAction extends AbstractAction<Definition> {

    private static final Logger log = LoggerFactory.getLogger(SaveFieldFormAction.class);

    protected EditorCallback callback;
    protected final EditorValidator validator;
    protected final JcrNodeAdapter item;

    public SaveFieldFormAction(Definition definition, JcrNodeAdapter item, EditorCallback callback, EditorValidator validator) {
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
                setProperty(node, "required", item);
                setProperty(node, "type", item);

                node.getSession().save();
            } catch (final RepositoryException e) {
                throw new ActionExecutionException(e);
            }
            callback.onSuccess(getDefinition().getName());
        } else {
            log.info("Validation error(s) occurred. No save performed.");
        }
    }

    private void setProperty(Node node, String propertyName, AbstractJcrNodeAdapter nodeAdapter) throws RepositoryException {
        com.vaadin.data.Property prop = nodeAdapter.getItemProperty(propertyName);
        if (prop == null || prop.getValue() == null) {
            // someone didn't set the property
            return;
        }
        try {
            node.setProperty(propertyName, StringUtils.trimToEmpty(((String) prop.getValue())));
        } catch (ClassCastException e) {
            node.setProperty(propertyName, (Boolean) prop.getValue());

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
            setImplementationClass(SaveFieldFormAction.class);
        }
    }
}
