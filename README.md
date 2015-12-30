Magnolia Neat Tweaks module
=======================

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) containing an [app](https://documentation.magnolia-cms.com/display/DOCS/Apps) for the [Magnolia CMS](http://www.magnolia-cms.com)

Neat Tweaks for Developers replaces existing JCR configuration module with something more user friendly.
Neat Tweaks for Editors changes default theme of Magnolia to give editors more freedom when working with Magnolia.
You can use one or both of the modules.

If you want to see tweaks delivered by module at work, please have a look at [Neat Tweaks for Developers](https://www.youtube.com/playlist?list=PLiOUpSP0-2XA_s1cO6Ao_u46-qnCIxja4) or [Neat Tweaks for Editors](https://www.youtube.com/playlist?list=PLiOUpSP0-2XA76i00oeyInVkuWW7TN5UT).

License
-------

Released under the GPLv3, see LICENSE.txt. 

Feel free to use this app, but if you modify the source code please fork us on Github.

Maven dependency
-----------------
```xml
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-editors</artifactId>
      <version>2.0.3</version>
    </dependency>
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-developers</artifactId>
      <version>2.0.3</version>
    </dependency>
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-commons</artifactId>
      <version>2.0.3</version>
    </dependency>
```

Versions
-----------------
Version 1.0.x compatible with Magnolia 5.3.x
Modules will install and most of the tweaks will also work with Magnolia 5.4, but sou will be definitely better of using 2.0.x version from master if you want to run tweaks on Magnolia 5.4.x. If you encounter any issues please report them back (ideally with attached patch :D ).

In case you want to just try out in existing installation without need to build everything, please download whole bundle and drop it in your WEB-INF/lib folder. Bundle can be found at https://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/com/neatresults/mgnltweaks/neat-tweaks-bundle/1.0.2/neat-tweaks-bundle-1.0.2.zip
or for Magnolia 5.4.x please use:
https://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/com/neatresults/mgnltweaks/neat-tweaks-bundle/2.0.2/neat-tweaks-bundle-2.0.2.zip

Installation & updates 
-----------------
To install the above tweaks successfully, you need to modify the theme of Magnolia. To do so, change your magnolia.ui.vaadin.theme property in magnolia.properties file to either neatcentral53 in Magnolia 5.3, or to neatcentral54 in Magnolia 5.4. If you are using a custom theme already, you can merge the two themes together and name the result neatcentral-anything-you-want.

Currently there are no update tasks for updates between different versions. If you are updating, you need to remove module first and perform full install. At the moment I'm not planning to add support for updates so don't bother reporting those as issues w/o providing patches at the same time. 

Changes
-----------------
2.0.3
- #6 prevent unnecessary reodering of modules on startup
- some utils for working with JCR API and Streams API of Java 8 (conversion of iterators in streams and so on).
- bootstrap custom image generator for STK only when STK is installed 

2.0.2
- fixed link to groovy script for app creation
- handle properties w/ null value correctly

2.0.1
- fixed issue with config subapp not being selected correctly when opening subapp from another app (pages)
- added action to list usages of given node throughout repository
- changed duplication of components in Pages app to place duplicated component directly after original instead of at the end
- fixed issue w/ empty selection in extends overview when no broken nodes are present
