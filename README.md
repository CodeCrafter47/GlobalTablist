GlobalTablist
=============

**Get back the global tablist**

This is a simple plugin for BungeeCord to get back the global userlist which has been disabled in the 1.8 protocol update. It just shows the players and allows for some header/footer. If you want more control over the tablist use my other plugin BungeeTabListPlus.

**Requirements:**

 * BungeeCord build #996 or above
 * Yamler

Commands
--------

 * `/globaltablist reload`
 
   Reloads the configuration file.
   
   Require permission: `globaltablist.admin`

Developer API
-------------

GlobalTablist provides an API which allows you to register custom placeholders.

**Maven:**
```xml
<repositories>
    <repository>
        <id>codecrafter47-repo</id>
        <url>http://nexus.codecrafter47.dyndns.eu/content/repositories/public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.codecrafter47.globaltablist</groupId>
        <artifactId>api</artifactId>
        <version>1.11</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Gradle:**
```groovy
repositories {
    maven { url "http://nexus.codecrafter47.dyndns.eu/content/repositories/public/" }
}

dependencies {
    compileOnly group: 'de.codecrafter47.globaltablist', name: 'api', version: '1.11'
}
```

**Creating a custom Placeholder:**

1. Add this to `bungee.yml`:

   ```yaml
   softdepend: ['GlobalTablist']
   ```

2. Create a class for your custom placeholder. The class must extend `de.codecrafter47.globaltablist.Placeholder`. Have a look at [the existing placeholders](https://github.com/CodeCrafter47/GlobalTablist/tree/master/src/main/java/codecrafter47/globaltablist/placeholders) to get an idea of how it works.

3. Register your custom placeholder in the `onEnable` method of your plugin:
   ```java
   public class MyPlugin extends Plugin {
   
       @Override
       public void onEnable() {
           GlobalTablist.getAPI().registerPlaceholder(this, new MyPlaceholder());
           // ...
       }
   }
   ```