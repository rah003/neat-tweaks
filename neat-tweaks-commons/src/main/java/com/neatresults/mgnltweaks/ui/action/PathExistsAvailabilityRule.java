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

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rule enabling action only if specified path exists. Can be property or node.
 */
public class PathExistsAvailabilityRule extends AbstractAvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(PathExistsAvailabilityRule.class);
    private Definition definition;

    public PathExistsAvailabilityRule(Definition definition) {
        this.definition = definition;
    }

    @Override
    protected boolean isAvailableForItem(Object itemId) {
        if (itemId instanceof JcrItemId && !(itemId instanceof JcrPropertyItemId)) {
            JcrItemId jcrItemId = (JcrItemId) itemId;
            return SessionUtil.getNode(definition.getWorkspace(), definition.getPath()) != null;
        }
        return false;
    }

    /**
     * Definition for outer class. Use it's path and workspace property to configure rule.
     */
    public static class Definition extends ConfiguredAvailabilityRuleDefinition {

        private String path;
        private String workspace;

        public Definition() {
            super.setImplementationClass(PathExistsAvailabilityRule.class);
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getWorkspace() {
            return workspace;
        }

        public void setWorkspace(String workspace) {
            this.workspace = workspace;
        }
    }
}
