* Migrate to the GTNH buildscript with Java 8 output, modern Java syntax support, and updated development tooling.
* Resolve AnimationAPI 1.2.4 through CurseMaven instead of bundling its source. Install AnimationAPI separately on both client and server.
* Generate the mod version from the build and configure Angelic Maven and release publishing.
* Add Catalogue Vintage artwork, license information, and an issue tracker link.
* Use the original CurseForge project image as the logo and a mutant-enderman screenshot as the Catalogue background.
* Include the upstream AGPLv3 license and update the README.
* Add a Chemical X mob list (`ChemicalXMobs`, flipped by `ChemicalXMobsIsWhitelist`). By default Chemical X only takes the mobs that have a mutant form, instead of killing any mob without a boss health bar.
* Feeding a creeper minion gunpowder makes it hiss contentedly (a CC0 recording), and feeding it TNT plays a short fuse.
* Add the hungry mutant zombie, for Special Mobs. Chemical X turns Special Mobs' hungry zombie into one, and it also spawns on its own (`SpawnHungryMutantZombie`). Its belly is eaten away down to the ribs and spine. It grabs its target, lifts it to its face and bites three times, eating food out of a player's inventory to heal, then flings it away. It walks over to food on the ground and eats it, even mid-fight if the food is close. What it eats falls out through its belly. It drops apples.
* Existing configs need `SpecialMobs.HungryZombie` added to `ChemicalXMobs` for Chemical X to take hungry zombies.
* Salamander is now required. The hungry mutant zombie is a GeckoLib model, driven by the mutant zombie's own animation code with GeckoLib clips for its own attacks.
