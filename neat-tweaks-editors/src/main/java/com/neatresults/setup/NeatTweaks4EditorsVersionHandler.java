package com.neatresults.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.resources.setup.InstallTextResourceTask;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class NeatTweaks4EditorsVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>(super.getExtraInstallTasks(installContext));
        tasks.add(new InstallTextResourceTask("/admincentral/custom_theme.css"));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/neatconfiguration", null,
                new CreateNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps", "neatconfiguration", NodeTypes.ContentNode.NAME)));
        tasks.add(new MoveNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/configuration", "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/configuration", false));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkSiteApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkSiteApp/subApps", "extends", "/modules/neat-tweaks/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkThemesApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkThemesApp/subApps", "extends", "/modules/neat-tweaks/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkChannelsApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkChannelsApp/subApps", "extends", "/modules/neat-tweaks/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkDialogsApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkDialogsApp/subApps", "extends", "/modules/neat-tweaks/apps/neatconfiguration/subApps")));
        tasks.add(new NodeExistsDelegateTask("", "/modules/ui-admincentral/apps/stkTemplateDefsApp/subApps",
                new SetPropertyTask("config", "/modules/ui-admincentral/apps/stkTemplateDefsApp/subApps", "extends", "/modules/neat-tweaks/apps/neatconfiguration/subApps")));
        return tasks;
    }
}