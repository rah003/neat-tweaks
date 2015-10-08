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

import info.magnolia.cms.license.LicenseFileExtractor;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractCondition;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Condition;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.resources.setup.InstallTextResourceTask;
import info.magnolia.objectfactory.Components;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Version handler for tweaks for editors.
 */
public abstract class DefaultNeatVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Condition> getInstallConditions() {
        List<Condition> conditions = new ArrayList<Condition>(super.getInstallConditions());
        conditions.add(new AbstractCondition("NeatCentral Theme check",
                "neatcentral theme must be configured in order to use functionality if this module succesfully. Please edit your magnolia.properties file and set magnolia.ui.vaadin.theme property to value \"neatcentral53\" or \"neatcentral54\" (w/o quotes) based on your Magnolia version.") {

            @Override
            public boolean check(InstallContext installContext) {
                MagnoliaConfigurationProperties props = Components.getComponent(MagnoliaConfigurationProperties.class);
                String theme = props.getProperty("magnolia.ui.vaadin.theme");
                return "neatcentral53".equals(theme) || "neatcentral54".equals(theme);
            }
        });
        return conditions;
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>(super.getExtraInstallTasks(installContext));
        if (!LicenseFileExtractor.getInstance().get(LicenseFileExtractor.VERSION_NUMBER).startsWith("5.4")) {
            tasks.add(new InstallTextResourceTask("/ui-admincentral/neat_theme.css"));
        } else {
            try {
                Session session = installContext.getJCRSession("resources");
                session.getRootNode().addNode("admincentral", NodeTypes.Folder.NAME);
                session.save();
            } catch (RepositoryException e) {
                e.printStackTrace();
                // ignore
            }
        }
        return tasks;
    }
    
    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        tasks.add(new IsModuleInstalledOrRegistered("remove old neatweaks if existit", "", "neat-tweaks",
                new ArrayDelegateTask("",
                          new RemoveNodeTask("","","config","/modules/neat-tweaks"),
                          new RemoveNodeTask("","","config","/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/neatconfiguration"),
                          new MoveNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/configuration", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/configuration", false)
                )));
        tasks.addAll(super.getBasicInstallTasks(installContext));
        return tasks;
    }
}