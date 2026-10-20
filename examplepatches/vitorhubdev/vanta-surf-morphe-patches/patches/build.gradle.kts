group = "app.morphe.patches.vantasurf"

patches {
    about {
        name = "Vanta Surf Patches"
        description = "Morphe patches for Vanta Surf: remove the ads, unlock the episodes and disable the telemetry."
        source = "https://github.com/vitorhubdev/vanta-surf-morphe-patches"
        author = "vitorhubdev"
        contact = "https://github.com/vitorhubdev/vanta-surf-morphe-patches/issues"
        website = "https://morphe-patches.software"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
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
}
