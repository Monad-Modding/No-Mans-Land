plugins {
    // https://projects.neoforged.net/neoforged/ModDevGradle
    id("net.neoforged.moddev") version "2.0.141"
    idea
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
}

version = mod["version"]
group = mod["group"]
base.archivesName = mod["id"]

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = neo["version"]

    validateAccessTransformers = true
    interfaceInjectionData.from(files("src/main/resources/META-INF/interfaceinjections.json"))

    parchment {
        mappingsVersion = project.parchment["mappingsVersion"]
        minecraftVersion = project.parchment["minecraftVersion"]
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }

        create("client") {
            client()
            devLogin = true
            jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
            jvmArgument("-XX:+AllowEnhancedClassRedefinition")
        }

        create("server") {
            server()
            programArgument("--nogui")
        }

        create("data") {
            data()
            programArguments.addAll(
                "--mod", mod["id"],
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
    }

    mods {
        create(mod["id"]) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main.get().resources {
    srcDir("src/generated/resources")
}

val localRuntime: Configuration by configurations.creating

configurations {
    runtimeClasspath {
        extendsFrom(localRuntime)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://cursemaven.com") {
                name = "CurseMaven"
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://mvn.devos.one/snapshots") {
                name = "Registrate"
            }
        }
        filter {
            includeGroup("com.tterrag.registrate")
        }
    }
    maven("https://maven.teamabnormals.com/")
    maven ("https://maven.blamejared.com/")
    maven("https://maven.ryanhcode.dev/releases")
    maven("https://maven.theillusivec4.top/")
    maven("https://maven.createmod.net")
    maven("https://maven.gegy.dev/releases")
    maven("https://maven.ithundxr.dev/snapshots")
    maven("https://maven.squiddev.cc") {
        name = "SquidDev Maven"
        content {
            includeGroup("cc.tweaked")
        }
    }
    flatDir { dirs("libs") }
}

dependencies {
    jarJar(implementation("maven.modrinth:mixed-litter:${deps["mixed_litter"]}") {})
    jarJar(api("dev.ryanhcode.sable-companion:sable-companion-common-${mc["version"]}:${deps["sable_companion"]}") {
        version {
            prefer(deps["sable_companion"])
        }
    })
    implementation("com.teamabnormals:blueprint:${mc["version"]}-${deps["blueprint"]}")

    implementation("dev.ryanhcode.sable:sable-neoforge-${mc["version"]}:${deps["sable"]}") { isTransitive = false }
    compileOnly("com.simibubi.create:create-${mc["version"]}:${deps["create"]}:slim") { isTransitive = false }

//MAKE SURE THESE ARE ALL "IMPLEMENTATION" WHEN RUNNING DATA! OTHERWISE DATA WILL BREAK!
    implementation("maven.modrinth:farmers-delight:1.21.1-1.3.0")
    implementation("maven.modrinth:boatload:6.0.2")
    implementation("maven.modrinth:the-block-box:0.1.1")
    implementation("maven.modrinth:nirvana-mod:DeFmDBVC")
    implementation("com.tterrag.registrate:Registrate:MC1.21-1.3.0+67")
    implementation("maven.modrinth:moonlight:1.21-2.29.18-neoforge")
//    implementation "maven.modrinth:spawn-mod:4.0.4"
    implementation("maven.modrinth:gallery:2.0.0")

    compileOnly("maven.modrinth:every-compat:1.21-2.11.28-neoforge")

    compileOnly("maven.modrinth:scaffolding-drops-nearby:1.21.1-3.4-fabric+forge+neo")
    compileOnly("maven.modrinth:collective:1.21.1-8.3-fabric+forge+neo")
    compileOnly("maven.modrinth:brewin-and-chewin:4.2.0+1.21.1-neoforge")
    compileOnly("maven.modrinth:serene-seasons:SPj5bJoM")
    compileOnly("maven.modrinth:glitchcore:8wmCpbQ2")
    compileOnly("maven.modrinth:supplementaries:1.21-3.5.27-neoforge")
    implementation("maven.modrinth:vanity-core:5.0.2")
    compileOnly("maven.modrinth:snow-real-magic:12.1.2+neoforge")
    compileOnly("maven.modrinth:etched:h6eJfwiW")
    compileOnly("maven.modrinth:stellarview:QD13LaRR")
    implementation("maven.modrinth:appleskin:3.0.5+mc1.21")

//    compileOnly("dev.lambdaurora.lambdynamiclights:lambdynamiclights-runtime:4.8.0+1.21.1")
    localRuntime("maven.modrinth:lambdynamiclights:4.8.10+1.21.1")

    compileOnly("mezz.jei:jei-${mc["version"]}-common-api:${deps["jei"]}")
    compileOnly("mezz.jei:jei-${mc["version"]}-neoforge-api:${deps["jei"]}")
    localRuntime("mezz.jei:jei-${mc["version"]}-neoforge:${deps["jei"]}")

//    localRuntime "maven.modrinth:xaeros-world-map:1.38.9_NeoForge_1.21"
    localRuntime("maven.modrinth:worldedit:7.3.6")
    localRuntime("maven.modrinth:sodium:mc1.21.1-0.8.12-beta.1-neoforge")
    //localRuntime "maven.modrinth:cyanide:v4LiMGGH"

//    implementation "maven.modrinth:DistantHorizonsApi:$distant_horizons_api_version"
    compileOnly("maven.modrinth:DistantHorizons:${deps["distant_horizons"]}-${mc["version"]}")
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
        "minecraft_version" to mc["version"],
        "minecraft_version_range" to mc["versionRange"],
        "neo_version" to neo["version"],
        "neo_version_range" to neo["versionRange"],
        "loader_version_range" to neo["loaderVersionRange"],
        "mod_id" to mod["id"],
        "mod_name" to mod["name"],
        "mod_license" to mod["license"],
        "mod_version" to mod["version"],
        "mod_authors" to mod["authors"],
        "mod_description" to mod["description"],
        "mixed_litter" to deps["mixed_litter"],
        "mixed_litter_range" to deps["mixed_litter_range"],
        "blueprint" to deps["blueprint"],
        "blueprint_range" to deps["blueprint_range"]
    )

    inputs.properties(replaceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

publishMods {
    type = STABLE
    file = tasks.jar.map { it.archiveFile.get() }
    changelog = provider { rootProject.file("CHANGELOG.md").readText() }

    version = mod["version"]
    displayName = "${mod["name"]} ${mod["version"]}"
    modLoaders.add("neoforge")

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = "538493"
        projectSlug = "no-mans-land"
        minecraftVersions.add(mc["version"])

        clientRequired = true
        serverRequired = true

        requires("blueprint")
        embeds("mixed-litter")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "kjZCvAn6"
        minecraftVersions.add(mc["version"])

        requires("blueprint")
        embeds("mixed-litter")
    }
}