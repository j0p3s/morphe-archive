plugins {
    alias(libs.plugins.android.library) apply false

    id("com.ncorti.ktfmt.gradle") version "0.27.0"
}

ktfmt { kotlinLangStyle() }
