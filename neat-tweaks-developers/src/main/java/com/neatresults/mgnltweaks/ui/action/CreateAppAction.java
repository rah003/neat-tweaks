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
package com.neatresults.mgnltweaks.ui.action;

import groovy.lang.Binding;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.module.groovy.console.MgnlGroovyConsole;
import info.magnolia.module.groovy.console.MgnlGroovyConsoleContext;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveConfigDialogActionDefinition;
import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Dialog action that saves result of the dialog as property rather than node.
 */
public class CreateAppAction extends SaveConfigDialogAction {

    private static final Logger log = LoggerFactory.getLogger(CreateAppAction.class);
    private UiContext uiContext;

    @Inject
    public CreateAppAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback, final @Named(SubAppEventBus.NAME) EventBus eventBus, UiContext uiContext) {
        super(definition, item, validator, callback, eventBus);
        this.uiContext = uiContext;
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
                    Node node = nodeAdapter.getJcrItem();

                    Context originalCtx = MgnlContext.getInstance();
                    InputStream inputStream = null;
                    MgnlGroovyConsoleContext groovyCtx = null;
                    try {

                        groovyCtx = new MgnlGroovyConsoleContext(originalCtx);
                        groovyCtx.put("appName", item.getItemProperty("appName").getValue());
                        String[] pathArray = StringUtils.split(node.getPath(), "/");
                        if (pathArray.length < 2) {
                            throw new ActionExecutionException("Can't create app on selected path: " + node.getPath());
                        }
                        groovyCtx.put("appLocation", pathArray[1]);

                        groovyCtx.put("appGroup", item.getItemProperty("appGroup").getValue());
                        groovyCtx.put("appIcon", StringUtils.defaultIfBlank((String) item.getItemProperty("appIcon").getValue(), "icon-items"));
                        groovyCtx.put("appRepository", StringUtils.defaultIfBlank((String) item.getItemProperty("appRepository").getValue(), "magnolia"));
                        groovyCtx.put("appFolderSupport", item.getItemProperty("appFolderSupport").getValue());

                        MgnlContext.setInstance(groovyCtx);
                        MgnlGroovyConsole console = new MgnlGroovyConsole(new Binding());

                        String inputFile = "/neat-tweaks-developers/appCreationScript.groovy";
                        // First Check
                        URL inFile = ClasspathResourcesUtil.getResource(inputFile);
                        if (inFile == null) {
                            throw new ActionExecutionException("Can't find resource file at " + inputFile);
                        }
                        // Get Input Stream
                        inputStream = ClasspathResourcesUtil.getResource(inputFile).openStream();
                        if (inputStream == null) {
                            throw new ActionExecutionException("Can't find resource file at " + inFile.getFile());
                        }

                        Writer writer = new StringWriter();

                        Object result = console.evaluate(inputStream, console.generateScriptName(), writer);

                        StringBuilder sb = new StringBuilder().append(writer.toString()).append("\n").append(result);
                        uiContext.openNotification(MessageStyleTypeEnum.INFO, true, sb.toString());

                    } finally {
                        // close jcr sessions
                        groovyCtx.release();
                        // close files
                        IOUtils.closeQuietly(inputStream);
                        // restore context
                        MgnlContext.setInstance(originalCtx);
                    }
                } catch (RepositoryException | IOException e) {
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
            super.setImplementationClass(CreateAppAction.class);
        }

    }
}
