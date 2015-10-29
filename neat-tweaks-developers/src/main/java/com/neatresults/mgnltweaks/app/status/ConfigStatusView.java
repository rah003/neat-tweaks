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
package com.neatresults.mgnltweaks.app.status;

import info.magnolia.ui.api.view.View;

import com.vaadin.data.Item;

/**
 * View for the config status subapp.
 */
public interface ConfigStatusView extends View {

    static final String EXTENDS_FAIL_COUNT = "extendsFailCount";
    static final String EXTENDS_COUNT = "extendsCount";
    static final String ABS_EXTENDS_COUNT = "absextendsCount";
    static final String REL_EXTENDS_COUNT = "relextendsCount";
    static final String OVR_EXTENDS_COUNT = "ovrextendsCount";
    static final String EXTENDS_FAIL_LIST = "extendsFailList";

    public void setListener(Listener listener);

    /**
     * Builds the view after setting it up.
     */
    public void build();

    /**
     * Listener for button actions.
     */
    public interface Listener {

        /**
         * Will refresh stats data.
         */
        void refreshData();

    }

    /**
     * Sets source of the stats data.
     */
    void setDataSource(Item item);

}
