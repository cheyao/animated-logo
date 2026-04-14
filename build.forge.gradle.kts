plugins {
	id("mod-platform")
	id("net.neoforged.moddev.legacyforge")
}

platform {
	loader = "forge"
	dependencies {
		required("minecraft") {
			forgeVersionRange = if (prop("deps.minecraft") == prop("deps.minecraft-max")) {
				"[${prop("deps.minecraft")}]"
			} else {
				"[${prop("deps.minecraft")}, ${prop("deps.minecraft-max")}]"
			}
		}
		required("forge") {
			forgeVersionRange = "[1,)"
		}
	}
}

legacyForge {
	version = "${property("deps.minecraft")}-${property("deps.forge")}"

	runs {
		register("client") {
			client()
			gameDirectory = file("run/")
			ideName = "Forge Client (${stonecutter.current.version})"
			programArgument("--username=Dev")
		}
	}


	mods {
		register(prop("mod.id")) {
			sourceSet(sourceSets["main"])
		}
	}
}

mixin {
	add(sourceSets.main.get(), "${prop("mod.id")}.mixins.refmap.json")
	config("${prop("mod.id")}.mixins.json")
}

repositories {
	mavenCentral()
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
}

dependencies {
	annotationProcessor("org.spongepowered:mixin:${libs.versions.mixin.get()}:processor")

	implementation(libs.moulberry.mixinconstraints)
	jarJar(libs.moulberry.mixinconstraints)
}

sourceSets {
	main {
		resources.srcDir(
			"${rootDir}/versions/datagen/${stonecutter.current.version.split("-")[0]}/src/main/generated"
		)
	}
}

tasks.named("createMinecraftArtifacts") {
	dependsOn(tasks.named("stonecutterGenerate"))
}

stonecutter {

}
