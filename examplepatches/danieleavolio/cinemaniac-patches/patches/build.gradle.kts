group = "app.cinemaniacpatches"

patches {
    about {
        name = "Cinemaniac Patches"
        description = "Ad blocking and privacy patches for the Cinemaniac movie tracker app."
        source = "https://github.com/danieleavolio/cinemaniac-patches"
        author = "danieleavolio"
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
