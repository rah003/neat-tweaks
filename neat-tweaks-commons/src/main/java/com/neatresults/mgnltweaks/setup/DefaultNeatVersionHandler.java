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
package com.neatresults.mgnltweaks.setup;

import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractCondition;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.Task;
import info.magnolia.module.resources.setup.InstallTextResourceTask;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for tweaks for editors.
 */
public abstract class DefaultNeatVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Condition> getInstallConditions() {
        List<Condition> conditions = new ArrayList<Condition>(super.getInstallConditions());
        conditions.add(new AbstractCondition("NeatCentral Theme check",
                "neatcentral theme must be configured in order to use functionality if this module succesfully. Please edit your magnolia.properties file and set magnolia.ui.vaadin.theme property to value \"neatcentral\" (w/o quotes).") {

            @Override
            public boolean check(InstallContext installContext) {
                MagnoliaConfigurationProperties props = Components.getComponent(MagnoliaConfigurationProperties.class);
                return "neatcentral".equals(props.getProperty("magnolia.ui.vaadin.theme"));
            }
        });
        return conditions;
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>(super.getExtraInstallTasks(installContext));
        tasks.add(new InstallTextResourceTask("/admincentral/custom_theme.css"));
        return tasks;
    }
}