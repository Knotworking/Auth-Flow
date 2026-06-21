plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.knotworking.authexample.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}
