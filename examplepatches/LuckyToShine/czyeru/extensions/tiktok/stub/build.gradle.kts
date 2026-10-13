plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.czyeru.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 22
    }
}

