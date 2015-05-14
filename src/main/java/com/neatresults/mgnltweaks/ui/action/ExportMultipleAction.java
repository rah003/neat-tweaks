package com.neatresults.mgnltweaks.ui.action;

import info.magnolia.cms.beans.config.MIMEMapping;
import info.magnolia.cms.core.Path;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.ExportCommand;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.ExportAction;
import info.magnolia.ui.framework.action.ExportActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;

/**
 * Action for multiple nodes in xml format.
 */
public class ExportMultipleAction extends ExportAction {
    private final Logger log = LoggerFactory.getLogger(ExportMultipleAction.class);
    private FileOutputStream fileOutputStream;
    private Map<String, File> tempFiles = new HashMap<String, File>();
    private int postExecCount;
    private File fileOutput;

    @Inject
    public ExportMultipleAction(Definition definition, List<JcrItemAdapter> items, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n) throws ActionExecutionException {
        super(definition, items.get(0), commandsManager, uiContext, i18n);
        // export action doesn't expose init for setting all items so we got to work around
        List<JcrItemAdapter> list = getItems();
        // first item would be already in the list, clear it out
        list.clear();
        list.addAll(items);
    }

    /**
     * Export each selected file in a temp file.
     */
    @Override
    protected void executeOnItem(JcrItemAdapter item) throws ActionExecutionException {
        try {
            ExportCommand exportCommand = (ExportCommand) getCommand();
            // Create a temporary file that will hold the data created by the export command.
            fileOutput = File.createTempFile(item.getItemId().getUuid(), exportCommand.getExt(), Path.getTempDirectory());

            String pathName = DataTransporter.createExportPath(item.getWorkspace() + item.getJcrItem().getPath());
            pathName = DataTransporter.encodePath(pathName, DataTransporter.DOT, DataTransporter.UTF8);

            tempFiles.put(pathName + exportCommand.getExt(), fileOutput);
            // Create a FileOutputStream link to the temporary file. The command use this FileOutputStream to populate data.
            fileOutputStream = new FileOutputStream(fileOutput);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionExecutionException("Not able to create a temporary file.", e);
        }
        super.executeOnItem(item);
    }

    /**
     * Once all items are exported, zip it all and send to client.
     */
    @Override
    protected void onPostExecute() throws Exception {
        IOUtils.closeQuietly(fileOutputStream);
        postExecCount++;

        String fileName;
        String mimeType;
        if (getItems().size() > 1 && postExecCount == getItems().size()) {
            fileName = "magnoliaExport.zip";
            mimeType = MIMEMapping.getMIMEType("zip");

            // will get deleted by stream after it is streamed through http request
            fileOutput = File.createTempFile("magnoliaExport", ".zip", Path.getTempDirectory());

            FileOutputStream fos = new FileOutputStream(fileOutput);
            ArchiveOutputStream zipOutput = new ArchiveStreamFactory().createArchiveOutputStream("zip", fos);
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
                IOUtils.closeQuietly(fos);
            }

        } else {
            ExportCommand exportCommand = (ExportCommand) getCommand();
            fileName = exportCommand.getFileName();
            mimeType = exportCommand.getMimeExtension();

        }
        if (postExecCount == getItems().size()) {
            openFileInBlankWindow(fileName, mimeType);
        }

    }

    @Override
    protected void openFileInBlankWindow(String fileName, String mimeType) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                try {
                    return new DeleteOnCloseFileInputStream(fileOutput);
                } catch (IOException e) {
                    log.warn("Not able to create an InputStream from the OutputStream. Return null", e);
                    return null;
                }
            }
        };
        StreamResource resource = new StreamResource(source, fileName);
        // Accessing the DownloadStream via getStream() will set its cacheTime to whatever is set in the parent
        // StreamResource. By default it is set to 1000 * 60 * 60 * 24, thus we have to override it beforehand.
        // A negative value or zero will disable caching of this stream.
        resource.setCacheTime(-1);
        resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");
        resource.setMIMEType(mimeType);

        Page.getCurrent().open(resource, "", true);
    }

    /**
     * Implementation of {@link FileInputStream} that ensure that the {@link File} <br>
     * used to construct this class is deleted on close() call.
     */
    private class DeleteOnCloseFileInputStream extends FileInputStream {
        private File file;
        private final Logger log = LoggerFactory.getLogger(DeleteOnCloseFileInputStream.class);

        public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (file.exists() && !file.delete()) {
                log.warn("Could not delete temporary export file {}", file.getAbsolutePath());
            }
        }

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
