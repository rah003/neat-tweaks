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
import info.magnolia.ui.admincentral.dialog.action.SaveDialogAction;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.neatresults.mgnltweaks.ui.action.SaveComponentOnTopDialogAction.Definition;
import com.vaadin.data.Item;

/**
 * SaveDialogAction that will position saved node as first sibling instead of default (last).
 */
public class SaveComponentOnTopDialogAction extends SaveDialogAction<Definition> {

    public SaveComponentOnTopDialogAction(Definition definition, Item item, EditorValidator validator, EditorCallback callback) {
        super(definition, item, validator, callback);
    }

    @Override
    protected void setNodeName(Node node, JcrNodeAdapter item) throws RepositoryException {
        super.setNodeName(node, item);
        // tricky thing. we need to reorder node after validation is checked and before callback is executed ...
        // ... the only place to do so short of reimplementing execute() is here
        NodeUtil.orderFirst(node);
    }

    /**
     * Definition for the class above.
     */
    public static class Definition extends SaveDialogActionDefinition {

        public Definition() {
            setImplementationClass(SaveComponentOnTopDialogAction.class);
        }

    }
}
