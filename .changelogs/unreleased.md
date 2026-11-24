* Migrate to the GTNH buildscript with Java 8 output, modern Java syntax support, and updated development tooling.
* Resolve AnimationAPI 1.2.4 through CurseMaven instead of bundling its source. Install AnimationAPI separately on both client and server.
* Generate the mod version from the build and configure Angelic Maven and release publishing.
* Add Catalogue Vintage artwork, license information, and an issue tracker link.
* Use the original CurseForge project image as the logo and a mutant-enderman screenshot as the Catalogue background.
* Include the upstream AGPLv3 license and update the README.
* Add a Chemical X mob list (`ChemicalXMobs`, flipped by `ChemicalXMobsIsWhitelist`). By default Chemical X only takes the mobs that have a mutant form, instead of killing any mob without a boss health bar.
* Feeding a creeper minion gunpowder makes it hiss contentedly (a CC0 recording), and feeding it TNT plays a short fuse.
* The mutant zombie no longer floats a little above the ground: its bent legs ended about a tenth of a block short.
* Add the mutant husk: the mutant zombie in a husk's colours, spawning in deserts (`SpawnMutantHusk`). Its slam and leap always leave the target hungry. With Et Futurum Requiem, Chemical X turns a husk into one and its roar raises husks; existing configs need `etfuturum.husk` added to `ChemicalXMobs`.
* Add the hungry mutant zombie, for Special Mobs. Chemical X turns Special Mobs' hungry zombie into one, and it also spawns on its own (`SpawnHungryMutantZombie`). Its belly is eaten away down to the ribs and spine. It grabs its target, lifts it to its face and bites three times, eating food out of a player's inventory to heal, then flings it away. It walks over to food on the ground and eats it, even mid-fight if the food is close. What it eats falls out through its belly. Its roar raises Special Mobs' hungry zombies. It drops apples.
* Add the fire mutant zombie, for Special Mobs. Chemical X turns Special Mobs' fire zombie into one, and it also spawns on its own in the Overworld and the Nether (`SpawnFireMutantZombie`). It is charred with glowing lava cracks, always aflame and immune to fire. Its ground slam and leap set targets alight, it breathes a cone of fire at targets three to nine blocks away, and its roar raises Special Mobs' fire zombies. Water and rain hurt it and put out its breath; while it lies dead, water finishes it instead of fire. It drops coal.
* Existing configs need `SpecialMobs.HungryZombie` and `SpecialMobs.FireZombie` added to `ChemicalXMobs` for Chemical X to take those zombies.
* Special Mobs zombies and Et Futurum husks raised by a mutant's roar act as its minions, as the mutant zombie's own do: they stay near it, take its target, ignore its ground slams and drop loot only a third of the time. Mixins into those mods add this, and it is saved with them.
* UniMixins is now required.
* Salamander is now required. The hungry mutant zombie is a GeckoLib model, driven by the mutant zombie's own animation code with GeckoLib clips for its own attacks.
