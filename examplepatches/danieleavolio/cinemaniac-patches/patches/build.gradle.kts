group = "app.daniele"

patches {
    about {
        name = "Cinemaniac Patches"
        description = "Unlock Pro for the Cinemaniac movie tracker app. " +
            "Patched by Morphe — support the original developer!"
        source = "https://github.com/danieleavolio/cinemaniac-patches"
        author = "Daniele Avolio"
        contact = "https://github.com/danieleavolio"
        website = "https://github.com/danieleavolio/cinemaniac-patches"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    publish {
        dependsOn("generatePatchesList")
    }
}
