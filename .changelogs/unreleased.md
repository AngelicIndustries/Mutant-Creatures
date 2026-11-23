* Migrate to the GTNH buildscript with Java 8 output, modern Java syntax support, and updated development tooling.
* Resolve AnimationAPI 1.2.4 through CurseMaven instead of bundling its source. Install AnimationAPI separately on both client and server.
* Generate the mod version from the build and configure Angelic Maven and release publishing.
* Add Catalogue Vintage artwork, license information, and an issue tracker link.
* Use the original CurseForge project image as the logo and a mutant-enderman screenshot as the Catalogue background.
* Include the upstream AGPLv3 license and update the README.
* Add a Chemical X mob list (`ChemicalXMobs`, flipped by `ChemicalXMobsIsWhitelist`). By default Chemical X only takes the mobs that have a mutant form, instead of killing any mob without a boss health bar.
* Feeding a creeper minion gunpowder makes it hiss contentedly (a CC0 recording), and feeding it TNT plays a short fuse.
