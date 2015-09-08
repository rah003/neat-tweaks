package com.neatresults.mgnltweaks.ui.field;

import static com.neatresults.mgnltweaks.ui.field.MgnlFilteringMode.CONTAINS;
import info.magnolia.beanmerger.BeanMergerUtil;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.OperationPermissionDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.templatingkit.sites.Site;
import info.magnolia.module.templatingkit.sites.SiteManager;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

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
    private String templateId;
    private SiteManager siteManager;

    @Inject
    public ComponentTemplateSelectFieldFactory(Definition definition, Item relatedFieldItem, SimpleTranslator i18n, MessagesManager oldi18n, TemplateDefinitionRegistry registry, SiteManager sm) {
        super(definition, relatedFieldItem);
        this.i18n = i18n;
        this.oldi18n = oldi18n;
        currentComponent = relatedFieldItem;
        this.registry = registry;
        this.siteManager = sm;
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

                ComponentAvailability availability = templateEntry.getValue();

                String id = availability.getId();
                TemplateDefinition def = registry.getTemplateDefinition(id);

                boolean skip = false;

                OperationPermissionDefinition perms = availability.getPermissions();
                if (!availability.isEnabled()) {
                    skip = true;
                }

                User user = MgnlContext.getUser();
                Collection<String> userRoles = user.getAllRoles();
                if (!availability.getRoles().isEmpty() & !CollectionUtils.containsAny(userRoles, availability.getRoles())) {
                    skip = true;
                }
                if (perms != null && !perms.canAdd(user)) {
                    skip = true;
                }

                if (!skip || id.equals(currentTemplateId)) {
                    SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
                    String label = def.getTitle();
                    if (StringUtils.isNotEmpty(label)) {
                        // i18n-ize
                        String newI18n = i18n.translate(label);
                        if (label.equals(newI18n)) {
                            // use old one (facepalm)
                            label = oldi18n.getMessages(def.getI18nBasename()).get(label);
                        } else {
                            label = newI18n;
                        }
                    } else {
                        label = id;
                    }
                    field.setLabel(label);
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
        if (fields.isEmpty()) {
            SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
            field.setName(definition.getName());
            field.setLabel("It would seem we failed to locate component substitutes available for this location. You might want to report this as an issue.");
            field.setValue(currentComponent.toString());
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
                Map<String, AreaDefinition> areaHierarchy = getAreaHierarchy(parentArea);

                return areaHierarchy.get(areaName).getAvailableComponents();
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

    private Map<String, AreaDefinition> getAreaHierarchy(Node parentArea) throws RepositoryException, RegistrationException {
        Map<String, AreaDefinition> areaHierarchy = new LinkedHashMap<String, AreaDefinition>();
        List<String> areaNamesHierarchy = new ArrayList<String> ();
        Node parentParentArea = parentArea;
        while (parentParentArea != null) {
            String areaName = parentParentArea.getName();
            areaNamesHierarchy.add(areaName);
            parentParentArea = NodeUtil.getNearestAncestorOfType(parentParentArea, NodeTypes.Area.NAME);
        }

        Node parentPage = NodeUtil.getNearestAncestorOfType(parentArea, NodeTypes.Page.NAME);
        templateId = parentPage.getProperty(NodeTypes.Renderable.TEMPLATE).getString();
        TemplateDefinition templateDef = registry.getTemplateDefinition(templateId);

        templateDef = mergeDefinition(templateDef);

        ListIterator<String> iter = areaNamesHierarchy.listIterator(areaNamesHierarchy.size());
        Node componentOrArea = parentPage;
        while (iter.hasPrevious()) {
            String name = iter.previous();
            componentOrArea = componentOrArea.getNode(name);
            // do we really need to merge here already?
            AreaDefinition area = templateDef.getAreas().get(name);
            if (area != null) {
                AreaDefinition areaDef = (AreaDefinition) mergeDefinition(area);

                if ("single".equals(areaDef.getType())) {
                    System.out.println(name);
                    System.out.println(areaDef.getId());
                    System.out.println(componentOrArea.hasNode("component"));
                    // from the area node get child called "component"
                    // from the component, get prop mgnl:template
                    // find component w/ matching id
                    // find if it has areas defined or if it is simply just component
                }
                // // what now? :D
                // areaDef.getAvailableComponents()
                // areaDef.getName()
                // //if component
                // .getParent().getParent().getPrimaryNodeType().getName()
                // // get name
                // ((JcrNodeAdapter) currentComponent).getJcrItem().getParent().getParent().getProperty("mgnl:template").getString()
                // // get definition for component
                // // and??
                // }

            } else {
                for (Entry<String, AreaDefinition> tempAreaEntry : templateDef.getAreas().entrySet()) {
                    AreaDefinition tempArea = tempAreaEntry.getValue();
                    AreaDefinition maybeHit = tempArea.getAreas().get(name);
                    if (maybeHit != null) {
                        areaHierarchy.put(tempAreaEntry.getKey(), tempAreaEntry.getValue());
                        templateDef = maybeHit;
                    }
                }
                // noComponent area ... how do i read those?
            }
            areaHierarchy.put(name, (AreaDefinition) templateDef);
        }

        return areaHierarchy;
    }

    private TemplateDefinition mergeDefinition(TemplateDefinition templateDef) {
        ConfiguredAreaDefinition areaDef = (ConfiguredAreaDefinition) templateDef;
        Site site = siteManager.getAssignedSite(((JcrNodeAdapter) currentComponent).getJcrItem());
        if (site == null) {
            return templateDef;
        }
        if (site.getTemplates().getPrototype() == null) {
            return templateDef;
        }
        AreaDefinition tempAreaPrototype = site.getTemplates().getPrototype().getArea(areaDef.getName());
        if (tempAreaPrototype == null) {
            return templateDef;
        }
        areaDef = BeanMergerUtil.merge(templateDef, tempAreaPrototype);
        return areaDef;
    }

    /**
     * Definition for custom select field.
     */
    public static class Definition extends SelectFieldDefinition {

        public Definition() {
            setReadOnly(false);
            setFilteringMode(CONTAINS);
        }
    }
}
