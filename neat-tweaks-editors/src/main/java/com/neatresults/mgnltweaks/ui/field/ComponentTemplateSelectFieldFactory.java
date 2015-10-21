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
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.lang.reflect.InvocationTargetException;
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
    private Object siteManager;

    @Inject
    public ComponentTemplateSelectFieldFactory(Definition definition, Item relatedFieldItem, SimpleTranslator i18n, MessagesManager oldi18n, TemplateDefinitionRegistry registry) {
        super(definition, relatedFieldItem);
        this.i18n = i18n;
        this.oldi18n = oldi18n;
        currentComponent = relatedFieldItem;
        this.registry = registry;
        try {
            this.siteManager = Components.getComponent(Class.forName("info.magnolia.module.templatingkit.sites.SiteManager"));
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            try {
                this.siteManager = Components.getComponent(Class.forName("info.magnolia.module.site.SiteManager"));
            } catch (ClassNotFoundException e1) {
                log.debug(e.getMessage(), e);
            }
        }
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
                Map<String, TemplateDefinition> areaHierarchy = getAreaHierarchy(parentArea);

                TemplateDefinition componentPageOrAreaDefinition = areaHierarchy.get(areaName);
                if (componentPageOrAreaDefinition instanceof AreaDefinition) {
                    return ((AreaDefinition) componentPageOrAreaDefinition).getAvailableComponents();

                } else if (componentPageOrAreaDefinition instanceof info.magnolia.module.templatingkit.templates.pages.STKPage) {
                    log.warn("found definition that is of type STKPage when looking for component availability");
                }
                return null;
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

    private Map<String, TemplateDefinition> getAreaHierarchy(Node parentArea) throws RepositoryException, RegistrationException {
        Map<String, TemplateDefinition> areaHierarchy = new LinkedHashMap<String, TemplateDefinition>();
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
            // subnode component is typically indication of having area type single
            if (!componentOrArea.hasNode(name) && (componentOrArea.hasNode("component") || (templateDef instanceof AreaDefinition && "single".equals(((AreaDefinition) templateDef).getType())))) {
                componentOrArea = componentOrArea.getNode("component/" + name);
                // so we know component is single, and we neeed to look if it has any sub areas
                String id = componentOrArea.getParent().getProperty(NodeTypes.Renderable.TEMPLATE).getString();
                TemplateDefinition componentDef = registry.getTemplateDefinition(id);
                if (componentDef != null) {
                    templateDef = componentDef;
                }
            } else {
                componentOrArea = componentOrArea.getNode(name);
            }
            // do we really need to merge here already?
            AreaDefinition area = templateDef.getAreas().get(name);
            if (area != null) {
                AreaDefinition areaDef = (AreaDefinition) mergeDefinition(area);
                templateDef = areaDef;
            } else {
                AreaDefinition maybeHit = templateDef.getAreas().get(name);
                if (maybeHit != null) {
                    areaHierarchy.put(name, maybeHit);
                    templateDef = maybeHit;
                } else {
                    // get subareas of the area? what the hack was i thinking when writing this? How does it work anyway?
                    for (Entry<String, AreaDefinition> tempAreaEntry : templateDef.getAreas().entrySet()) {
                        AreaDefinition tempArea = tempAreaEntry.getValue();
                        maybeHit = tempArea.getAreas().get(name);
                        if (maybeHit != null) {
                            areaHierarchy.put(tempAreaEntry.getKey(), tempAreaEntry.getValue());
                            templateDef = maybeHit;
                        }
                    }
                }
                // noComponent area ... how do i read those?
            }
            areaHierarchy.put(name, templateDef);
        }

        return areaHierarchy;
    }

    private TemplateDefinition mergeDefinition(TemplateDefinition templateDef) {
        Node jcrItem = ((JcrNodeAdapter) currentComponent).getJcrItem();
        Object site = null;
        // yes reflection because that class was changed between 5.3 and 5.4 ... feel free to fork and use normal code for your major version. I'm not maintaining two branches.
        try {
            site = siteManager.getClass().getMethod("getAssignedSite", Node.class).invoke(siteManager, jcrItem);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            log.error(e.getMessage(), e);
        }

        if (site == null) {
            return templateDef;
        }
        Object templates;
        TemplateDefinition prototype = null;
        try {
            templates = site.getClass().getMethod("getTemplates").invoke(site);
            prototype = (TemplateDefinition) templates.getClass().getMethod("getPrototype").invoke(templates);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            log.error(e.getMessage(), e);
        }
        if (prototype == null) {
            return templateDef;
        }
        AreaDefinition tempAreaPrototype = ((ConfiguredTemplateDefinition) prototype).getAreas().get(templateDef.getName());
        if (tempAreaPrototype == null) {
            return templateDef;
        }
        ConfiguredAreaDefinition areaDef = BeanMergerUtil.merge(templateDef, tempAreaPrototype);
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
