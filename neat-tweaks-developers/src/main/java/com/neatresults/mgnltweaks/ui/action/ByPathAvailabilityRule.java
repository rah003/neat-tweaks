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
