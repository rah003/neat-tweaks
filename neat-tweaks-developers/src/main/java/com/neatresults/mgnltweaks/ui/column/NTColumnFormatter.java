package com.neatresults.mgnltweaks.ui.column;

import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.column.NTColumnFormatter.Definition;
import com.vaadin.ui.Table;

/**
 * Simple column formatter displaying primary node types for nodes and property type for properties. Useful mainly for debugging and administration.
 */
public class NTColumnFormatter extends AbstractColumnFormatter<Definition> implements ColumnFormatter {

    private static final Logger log = LoggerFactory.getLogger(NTColumnFormatter.class);

    @Inject
    public NTColumnFormatter(Definition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        final Item jcrItem = getJcrItem(source, itemId);
        try {
            if (jcrItem == null) {
                return StringUtils.EMPTY;
            } else if (jcrItem.isNode()) {
                Node node = (Node) jcrItem;
                return "[" + node.getPrimaryNodeType().getName() + "]";
            } else {
                javax.jcr.Property property = (javax.jcr.Property) jcrItem;
                return PropertyType.nameFromValue(property.getType());
            }
        } catch (RepositoryException e) {
            log.warn("Unable to get the displayed node type for the {}", itemId, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Definition for the formatter.
     */
    public static class Definition extends AbstractColumnDefinition {
        public Definition() {
            setFormatterClass(NTColumnFormatter.class);
        }
    }
}
