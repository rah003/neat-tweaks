package com.neatresults.mgnltweaks.ui.column;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;

/**
 * Simple column formatter displaying primary node types for nodes and property type for properties. Useful mainly for debugging and administration.
 */
public class LinkToPathColumnFormatter extends AbstractColumnFormatter<ColumnDefinition> implements ColumnFormatter {

    private static final Logger log = LoggerFactory.getLogger(LinkToPathColumnFormatter.class);
    private LocationController locationController;
    private EventBus eventBus;

    @Inject
    public LinkToPathColumnFormatter(Definition definition, LocationController locationController, @Named(AdmincentralEventBus.NAME) final EventBus eventBus) {
        super(definition);
        this.locationController = locationController;
        this.eventBus = eventBus;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        Item item = source.getItem(itemId);
        Property prop = (item == null) ? null : item.getItemProperty(columnId);

        // Need to check prop.getValue() before prop.getType() to avoid npe if value is null.
        if (prop != null && prop.getValue() != null && prop.getType().equals(String.class)) {
            String path = (String) prop.getValue();

            if (StringUtils.isNotBlank(path) && item instanceof JcrPropertyAdapter) {
                JcrPropertyAdapter jcrProp = (JcrPropertyAdapter) item;
                String propName = null;
                try {
                    propName = jcrProp.getJcrItem().getName();
                } catch (RepositoryException e) {
                    log.debug(e.getMessage(), e);
                }
                if ("templateScript".equals(propName) && path.startsWith("/") && path.endsWith(".ftl")) {
                    return createLinkButton("templates", path);
                } else if ("dialog".equals(propName)) {
                    String title = path;
                    String[] parts = path.split(":");
                    path = "/modules/" + parts[0] + "/dialogs/" + parts[1];
                    return createLinkButton(title, "config", path);
                } else if ("extends".equals(propName)) {
                    String title = path;
                    if (!path.startsWith("/")) {
                        try {
                            path = jcrProp.getJcrItem().getParent().getPath() + "/" + path;
                        } catch (RepositoryException e) {
                            log.debug(e.getMessage(), e);
                            // switch back to original path in case it doesn't exist to prevent accidental conversion of relative to absolute
                            path = title;
                        }
                    }
                    return createLinkButton(title, "config", path);
                }
            }
            return path;
        }

        return null;
    }

    private Object createLinkButton(String workspace, String path) {
        return createLinkButton(path, workspace, path);
    }
    private Object createLinkButton(String title, String workspace, String path) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            String appName = null;
            if ("templates".equals(workspace) && session.nodeExists(StringUtils.substringBeforeLast(path, "."))) {
                appName = "inplace-templating";
                return createButton(appName, "detail", path, new JcrNodeItemId(session.getNode(StringUtils.substringBeforeLast(path, ".")).getIdentifier(), workspace));
            } else if ("config".equals(workspace) && session.nodeExists(path)) {
                appName = "configuration";
                return createButton(title, appName, "browser", path, new JcrNodeItemId(session.getNode(path).getIdentifier(), workspace));
            }
        } catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
        }
        // need to return title (original value) as path might have been trasnformed already
        return title;
    }

    private Button createButton(final String appName, final String subAppName, final String path, Object itemId) {
        return createButton(path, appName, subAppName, path, itemId);
    }

    private Button createButton(final String title, final String appName, final String subAppName, final String path, final Object itemId) {
        Button selectButton = new NativeButton();
        selectButton.addStyleName("neatmagnoliabutton");
        selectButton.setCaption(title);
        selectButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                Location location = null;
                location = new DefaultLocation("app", appName, subAppName, path);
                locationController.goTo(location);
            }
        });
        return selectButton;
    }

    /**
     * Definition for the formatter.
     */
    public static class Definition extends MetaDataColumnDefinition {
        public Definition() {
            setFormatterClass(LinkToPathColumnFormatter.class);
        }
    }

}
