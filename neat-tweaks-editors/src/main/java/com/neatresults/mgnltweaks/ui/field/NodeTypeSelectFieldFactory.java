package com.neatresults.mgnltweaks.ui.field;

import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionProvider;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.field.NodeTypeSelectFieldFactory.Definition;
import com.vaadin.data.Item;

/**
 * Select field retrieving dynamically all Dialog IDs from config workspace.
 */
public class NodeTypeSelectFieldFactory extends SelectFieldFactory<Definition> {

    private static final Logger log = LoggerFactory.getLogger(NodeTypeSelectFieldFactory.class);
    private SimpleTranslator i18n;
    private MessagesManager oldi18n;
    private RepositoryManager repoMan;
    private DialogDefinitionRegistry registry;

    @Inject
    public NodeTypeSelectFieldFactory(Definition definition, Item relatedFieldItem, SimpleTranslator i18n, MessagesManager oldi18n, RepositoryManager repoMan, DialogDefinitionRegistry registry) {
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
            Field registryField = registry.getClass().getDeclaredField("registry");
            registryField.setAccessible(true);
            RegistryMap<String, DialogDefinitionProvider> registryMap = (RegistryMap<String, DialogDefinitionProvider>) registryField.get(this.registry);

            for (String id : registryMap.keySet()) {

                SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
                FormDialogDefinition dialogDef = registryMap.get(id).getDialogDefinition();
                // directly defined label
                String label = dialogDef.getLabel();

                if (label == null) {
                    // new i18n maybe?
                    String name = dialogDef.getId().indexOf("/") > 0 ? StringUtils.substringAfterLast(dialogDef.getId(), "/") : StringUtils.substringAfterLast(dialogDef.getId(), ":");
                    String key = StringUtils.substringBefore(dialogDef.getId(), ":") + "." + name + ".label";
                    label = i18n.translate(key);
                    // old i18n maybe?
                    if (key.equals(label)) {
                        // no .level suffix maybe?
                        label = i18n.translate(StringUtils.substringBeforeLast(key, "."));
                        if (StringUtils.substringBeforeLast(key, ".").equals(label)) {
                            String oldFormLabel = dialogDef.getForm().getLabel();
                            if (StringUtils.isNotBlank(oldFormLabel)) {
                                label = oldi18n.getMessages(dialogDef.getForm().getI18nBasename()).get(oldFormLabel);
                            } else {
                                // some weird guessing
                                label = oldi18n.getMessages(dialogDef.getForm().getI18nBasename()).get(key);
                                if (label.startsWith("???")) {
                                    // one last try - pbly old not translated dialog, get label from first tab
                                    List<TabDefinition> tabs = dialogDef.getForm().getTabs();
                                    if (tabs.size() > 0) {
                                        label = tabs.get(0).getLabel();
                                    }
                                }
                            }
                        }
                    }
                }
                field.setLabel(id + " (" + label + ")");
                field.setName(definition.getName());
                field.setValue(id);
                fields.add(field);
            }

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | RegistrationException | NoSuchFieldException e) {
            log.error(e.getMessage(), e);
            SelectFieldOptionDefinition field = new SelectFieldOptionDefinition();
            field.setName("It looks like an error has occured. Please contact admin or developers about it: " + e.getMessage());
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
        }
    }
}
