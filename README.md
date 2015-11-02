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
      <version>2.0.1</version>
    </dependency>
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-developers</artifactId>
      <version>2.0.1</version>
    </dependency>
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-commons</artifactId>
      <version>2.0.1</version>
    </dependency>
```

Versions
-----------------
Version 1.0.x compatible with Magnolia 5.3.x
Modules will install and most of the tweaks will also work with Magnolia 5.4, but sou will be definitely better of using 2.0.x version from master if you want to run tweaks on Magnolia 5.4.x. If you encounter any issues please report them back (ideally with attached patch :D ).

In case you want to just try out in existing installation without need to build everything, please download whole bundle and drop it in your WEB-INF/lib folder. Bundle can be found at https://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/com/neatresults/mgnltweaks/neat-tweaks-bundle/1.0.2/neat-tweaks-bundle-1.0.2.zip
or for Magnolia 5.4.x please use:
https://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/com/neatresults/mgnltweaks/neat-tweaks-bundle/2.0.1/neat-tweaks-bundle-2.0.1.zip

Installation & updates 
-----------------
Currently there are no update tasks for updates between different versions. If you are updating, you need to remove module first and perform full install. At the moment I'm not planning to add support for updates so don't bother reporting those as issues w/o providing patches at the same time. 
