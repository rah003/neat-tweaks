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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class NeatTweaks4DevelopersVersionHandler extends DefaultNeatVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>(super.getExtraInstallTasks(installContext));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/neatconfiguration", null,
                new CreateNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps", "neatconfiguration", NodeTypes.ContentNode.NAME)));
        tasks.add(new MoveNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/configuration", "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/configuration", false));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkSiteApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkSiteApp/subApps", "extends", "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkThemesApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkThemesApp/subApps", "extends", "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkChannelsApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkChannelsApp/subApps", "extends", "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkDialogsApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkDialogsApp/subApps", "extends", "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkTemplateDefsApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkTemplateDefsApp/subApps", "extends", "/modules/neat-tweaks-developers/apps/neatconfiguration/subApps")));
        return tasks;
    }
    
}