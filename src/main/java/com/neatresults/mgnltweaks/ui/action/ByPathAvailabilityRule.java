package com.neatresults.mgnltweaks.ui.action;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ByPathAvailabilityRule using regex property from definition allows limiting availability by path.
 */
public class ByPathAvailabilityRule extends AbstractAvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(ByPathAvailabilityRule.class);
    private Definition definition;

    public ByPathAvailabilityRule(Definition definition) {
        this.definition = definition;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        if (itemId instanceof JcrItemId && !(itemId instanceof JcrPropertyItemId)) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            Node node = SessionUtil.getNodeByIdentifier(jcrItemId.getWorkspace(), jcrItemId.getUuid());
            if (node != null) {
                try {
                    return node.getPath().matches(definition.getRegex());
                } catch (RepositoryException e) {
                    log.warn("Error evaluating availability for node [{}], returning false: {}", NodeUtil.getPathIfPossible(node), e.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Definition for outer class. Use it's regex property to configure rule.
     */
    public static class Definition extends ConfiguredAvailabilityRuleDefinition {

        private String regex;

        public Definition() {
            super.setImplementationClass(ByPathAvailabilityRule.class);
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }
    }
}
