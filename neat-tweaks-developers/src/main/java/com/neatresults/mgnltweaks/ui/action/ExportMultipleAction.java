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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.jcr.Item;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.ExportCommand;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.AbstractCommandAction;
import info.magnolia.ui.framework.action.ExportAction;
import info.magnolia.ui.framework.action.ExportActionDefinition;
import info.magnolia.ui.framework.util.TempFileStreamResource;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

/**
 * Action for multiple nodes in xml format.
 */
public class ExportMultipleAction extends AbstractCommandAction<ExportActionDefinition> {
    private final Logger log = LoggerFactory.getLogger(ExportMultipleAction.class);
    private FileOutputStream fileOutputStream;
    private Map<String, File> tempFiles = new HashMap<String, File>();
    private int postExecCount;
    private File fileOutput;
    private TempFileStreamResource tempFileStreamResource;

    @Inject
    public ExportMultipleAction(Definition definition, List<JcrItemAdapter> items, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) throws ActionExecutionException {
        super(definition, items.get(0), commandsManager, uiContext, i18n);
        // export action doesn't expose init for setting all items so we got to work around
        // by getting pointer to a list of items in AbstractCommandAction
        List<JcrItemAdapter> list = getItems();
        // first item would be already in the list, clear it out
        list.clear();
        // and adding all items to that list manually.
        list.addAll(items);
        // and pray that noone ever changes ACA to return copy of item list instead
    }

    @Override
    protected void onPreExecute() throws Exception {
        if (getItems().size() > 1) {
            fileOutput = File.createTempFile(getCurrentItem().getItemId().getUuid(), ".xml");
        } else {
            tempFileStreamResource = new TempFileStreamResource();
            tempFileStreamResource.setTempFileName(getCurrentItem().getItemId().getUuid());
            tempFileStreamResource.setTempFileExtension("xml");
        }
        // gotta call super or buildParam() will never get called and thus final getParam() will fail w/ NPE (facepalm)
        super.onPreExecute();
    }

    /**
     * Once all items are exported, zip it all and send to client.
     */
    @Override
    protected void onPostExecute() throws Exception {
        IOUtils.closeQuietly(fileOutputStream);
        postExecCount++;

        if (getItems().size() > 1) {
            ExportCommand exportCommand = (ExportCommand) getCommand();
            String fileName = exportCommand.getFileName();
            tempFiles.put(fileName, fileOutput);

        }
        if (getItems().size() > 1 && postExecCount == getItems().size()) {
            // last run on multi-run
            String fileName = "magnoliaExport.zip";
            String mimeType = MIMEMapping.getMIMEType("zip");


            // will get deleted by stream after it is streamed through http request
            TempFileStreamResource tempFileStreamResource = new TempFileStreamResource();
            tempFileStreamResource.setTempFileName("magnoliaExport" + (Math.random() * 1000));
            tempFileStreamResource.setTempFileExtension("zip");
            ArchiveOutputStream zipOutput = new ArchiveStreamFactory().createArchiveOutputStream("zip", tempFileStreamResource.getTempFileOutputStream());
            try {

                for (Entry<String, File> entry : tempFiles.entrySet()) {
                    zipOutput.putArchiveEntry(new ZipArchiveEntry(entry.getValue(), entry.getKey()));
                    FileInputStream fis = new FileInputStream(entry.getValue());
                    IOUtils.copy(fis, zipOutput);
                    zipOutput.closeArchiveEntry();
                    IOUtils.closeQuietly(fis);
                }
            } finally {
                zipOutput.finish();
                IOUtils.closeQuietly(zipOutput);
            }
            tempFileStreamResource.setFilename(fileName);
            tempFileStreamResource.setMIMEType(mimeType);
            // is this really necessary?
            tempFileStreamResource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");
            // Opens the resource for download
            Page.getCurrent().open(tempFileStreamResource, "", true);

        } else if (getItems().size() == 1) {
            // single item run
            final ExportCommand exportCommand = (ExportCommand) getCommand();
            tempFileStreamResource.setFilename(exportCommand.getFileName());
            tempFileStreamResource.setMIMEType(exportCommand.getMimeExtension());
            // Opens the resource for download
            Page.getCurrent().open(tempFileStreamResource, "", true);
        }
    }

    @Override
    protected Map<String, Object> buildParams(final Item jcrItem) {
        Map<String, Object> params = super.buildParams(jcrItem);
        params.put(ExportCommand.EXPORT_EXTENSION, ".xml");
        params.put(ExportCommand.EXPORT_FORMAT, Boolean.TRUE);
        params.put(ExportCommand.EXPORT_KEEP_HISTORY, Boolean.FALSE);
        try {
            if (getItems().size() > 1) {
                params.put(ExportCommand.EXPORT_OUTPUT_STREAM, new FileOutputStream(fileOutput));
            } else {
                params.put(ExportCommand.EXPORT_OUTPUT_STREAM, tempFileStreamResource.getTempFileOutputStream());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to bind command to temp file output stream: ", e);
        }
        return params;
    }

    /**
     * Once all items are exported, zip it all and send to client.
     */
    @Override
    protected void onError(Exception e) {
        IOUtils.closeQuietly(fileOutputStream);
        log.error("Failed to export multiple items with {}", e.getMessage(), e);
        super.onError(e);
    }

    /**
     * Definition for the outer class.
     */
    public static class Definition extends ExportActionDefinition {

        public Definition() {
            setImplementationClass(ExportAction.class);
        }
    }
}
