import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runServer")) {
        doFirst("prepareWawelAuthDevConfig") {
            // Fresh checkouts and CI need a local endpoint; never replace an existing config.
            val configFile = workingDir.resolve("config/wawelauth/server.json")
            if (!configFile.exists()) {
                configFile.parentFile.mkdirs()
                configFile.writeText("""
                    {
                      "publicBaseUrl": "http://localhost:25565/",
                      "enableLocalAuth": false,
                      "enablePublicPage": false,
                      "enablePublicInfoApi": false
                    }
                """.trimIndent() + "\n")
            }
        }
        doFirst("stripClientOnlyMods") {
            val clientOnlyModIds = listOf("angelica", "ModularUI2", "Baubles-Expanded", "catalogue")
            classpath = classpath.filter { file ->
                clientOnlyModIds.none { file.name.contains(it, ignoreCase = true) }
            }
        }
    }
}

extensions.configure<PublishingExtension> {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Mutant Creatures")
            description.set("Maintained fork of Mutant Creatures for Minecraft 1.7.10.")
            url.set("https://github.com/AngelicIndustries/Mutant-Creatures")
            organization {
                name.set("Angelic Industries")
                url.set("https://angelic.industries/")
            }
            scm {
                url.set("https://github.com/AngelicIndustries/Mutant-Creatures")
                connection.set("scm:git:https://github.com/AngelicIndustries/Mutant-Creatures.git")
                developerConnection.set("scm:git:ssh://git@github.com/AngelicIndustries/Mutant-Creatures.git")
            }
        }
    }
}
