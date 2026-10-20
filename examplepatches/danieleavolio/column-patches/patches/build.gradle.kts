group = "app.columnpatches"

patches {
    about {
        name = "Column Patches"
        description = "Ad/tracker blocking and privacy patches for the Column news app."
        source = "https://github.com/danieleavolio/column-patches"
        author = "danieleavolio"
        contact = "https://github.com/danieleavolio"
        website = "https://github.com/danieleavolio/column-patches"
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
