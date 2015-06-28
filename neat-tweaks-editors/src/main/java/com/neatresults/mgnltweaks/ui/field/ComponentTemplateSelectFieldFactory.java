package com.neatresults.mgnltweaks.ui.field;

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.OperationPermissionDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.field.ComponentTemplateSelectFieldFactory.Definition;
import com.vaadin.data.Item;

/**
 * Select field retrieving dynamically all Dialog IDs from config workspace.
 */
public class ComponentTemplateSelectFieldFactory extends SelectFieldFactory<Definition> {

    private static final Logger log = LoggerFactory.getLogger(ComponentTemplateSelectFieldFactory.class);
    private SimpleTranslator i18n;
    private MessagesManager oldi18n;
    private Item currentComponent;
    private TemplateDefinitionRegistry registry;

    @Inject
    public ComponentTemplateSelectFieldFactory(Definition definition, Item relatedFieldItem, SimpleTranslator i18n, MessagesManager oldi18n, TemplateDefinitionRegistry registry) {
        super(definition, relatedFieldItem);
        this.i18n = i18n;
        this.oldi18n = oldi18n;
        currentComponent = relatedFieldItem;
        this.registry = registry;
    }

    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {

        List<SelectFieldOptionDefinition> fields = new ArrayList<SelectFieldOptionDefinition>();
        try {
            Map<String, ComponentAvailability> temlatesAvailableInParentArea = getParentAreaTemplates(currentComponent);

            if (temlatesAvailableInParentArea == null) {
                // add itself?
                return fields;
            }

            String currentTemplateId = (String) currentComponent.getItemProperty(NodeTypes.Renderable.TEMPLATE).getValue();

            for (Map.Entry<String, ComponentAvailability> templateEntry : temlatesAvailableInParentArea.entrySet()) {

                String id = templateEntry.getKey();
                TemplateDefinition def = registry.getTemplateDefinition(templateEntry.getKey());

                boolean skip = false;

                ComponentAvailability availability = templateEntry.getValue();
                OperationPermissionDefinition perms = availability.getPermissions();
                if (!availability.isEnabled()) {
                    skip = true;
                }

                User user = MgnlContext.getUser();
                Collection<String> userRoles = user.getAllRoles();
                if (!availability.getRoles().isEmpty() & !CollectionUtils.containsAny(userRoles, availability.getRoles())) {
                    skip = true;
                }
                if (!perms.canAdd(user)) {
                    skip = true;
                }

                if (!skip || id.equals(currentTemplateId)) {
                    SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
                    String label = def.getTitle();
                    field.setLabel(id + (StringUtils.isEmpty(label) ? "" : (" (" + label + ")")));
                    field.setName(definition.getName());
                    field.setValue(id);
                    fields.add(field);
                }
            }

        } catch (SecurityException | IllegalArgumentException | RegistrationException e) {
            log.error(e.getMessage(), e);
            SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
            field.setName(definition.getName());
            field.setLabel("It looks like an error has occured. Please contact admin or developers about it: " + e.getMessage());
            field.setValue(e.getMessage());
            fields.add(field);

        }
        return fields;
    }


    private Map<String, ComponentAvailability> getParentAreaTemplates(Item currentComponent) {
        if (currentComponent instanceof JcrNodeAdapter) {
            String templateId = null;
            try {
                Node component = ((JcrNodeAdapter) currentComponent).getJcrItem();
                Node parentArea = NodeUtil.getNearestAncestorOfType(component, NodeTypes.Area.NAME);
                String areaName = parentArea.getName();
                Node parentPage = NodeUtil.getNearestAncestorOfType(component, NodeTypes.Page.NAME);
                templateId = parentPage.getProperty(NodeTypes.Renderable.TEMPLATE).getString();
                TemplateDefinition templateDef;
                templateDef = registry.getTemplateDefinition(templateId);
                Node parentParentArea = NodeUtil.getNearestAncestorOfType(parentArea, NodeTypes.Area.NAME);
                if (parentParentArea != null) {
                    areaName = parentParentArea.getName();
                }
                // fuck that - go up through all areas and then down again via definitions
                return templateDef.getAreas().get(areaName).getAvailableComponents();
            } catch (RepositoryException e) {
                // failed to access repo :(
                log.error("Failed to locate template id for {}", currentComponent, e);
            } catch (RegistrationException e) {
                // template itself is not registered anymore
                log.error("Failed to load template definition for {}", templateId, e);
            }
        }
        return null;
    }

    /**
     * Definition for custom select field.
     */
    public static class Definition extends SelectFieldDefinition {

        public Definition() {
            setReadOnly(false);
            setFilteringMode(MgnlFilteringMode.CONTAINS);
        }
    }
}
