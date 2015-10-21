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

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.field.FieldTypeSelectFieldFactory.Definition;
import com.vaadin.data.Item;

/**
 * Select field retrieving dynamically all Dialog IDs from config workspace.
 */
public class FieldTypeSelectFieldFactory extends SelectFieldFactory<Definition> {

    private static final Logger log = LoggerFactory.getLogger(FieldTypeSelectFieldFactory.class);
    private SimpleTranslator i18n;
    private MessagesManager oldi18n;
    private RepositoryManager repoMan;
    private FieldTypeDefinitionRegistry registry;

    @Inject
    public FieldTypeSelectFieldFactory(Definition definition, Item relatedFieldItem, SimpleTranslator i18n, MessagesManager oldi18n, RepositoryManager repoMan, FieldTypeDefinitionRegistry registry) {
        super(definition, relatedFieldItem);
        this.i18n = i18n;
        this.oldi18n = oldi18n;
        this.repoMan = repoMan;
        this.registry = registry;
    }

    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {

        List<SelectFieldOptionDefinition> fields = new ArrayList<SelectFieldOptionDefinition>();
        try {
            if (registry.getClass().getDeclaredFields().length == 0) {
                // 5.4
                Collection<FieldTypeDefinition> defs = (Collection<FieldTypeDefinition>) registry.getClass().getMethod("getAllDefinitions").invoke(registry, null);
                for (FieldTypeDefinition fieldDef : defs) {
                    if (fieldDef == null || fieldDef.getDefinitionClass() == null || fieldDef.getDefinitionClass().getName() == null) {
                        System.out.println("field def is not valid: " + fieldDef);
                        continue;
                    }
                    SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
                    field.setLabel(fieldDef.getDefinitionClass().getName());
                    field.setName(definition.getName());
                    field.setValue(fieldDef.getDefinitionClass().getName());
                    fields.add(field);
                }
            } else {
                // 5.3
                Field registryField = registry.getClass().getDeclaredField("registry");
                registryField.setAccessible(true);
                RegistryMap<String, FieldTypeDefinitionProvider> providers = (RegistryMap<String, FieldTypeDefinitionProvider>) registryField.get(registry);

                for (String id : providers.keySet()) {

                    SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
                    Class<? extends FieldDefinition> fieldDef = registry.get(id).getDefinitionClass();
                    // directly defined label
                    String label = id + " (" + StringUtils.replaceOnce(fieldDef.getName(), "info.magnolia.module.", "i.m.m.") + ")";

                    field.setLabel(label);
                    field.setName(definition.getName());
                    field.setValue(fieldDef.getName());
                    fields.add(field);
                }
            }

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | RegistrationException | NoSuchFieldException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
            field.setName(definition.getName());
            field.setLabel("It looks like an error has occured. Please contact admin or developers about it: " + e.getMessage());
            field.setValue(e.getMessage());
            fields.add(field);

        }
        return fields;
    }

    /**
     * Definition for custom select field.
     */
    public static class Definition extends SelectFieldDefinition {

        public Definition() {
            setFilteringMode(2);
        }
    }
}
