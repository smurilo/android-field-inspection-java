plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.smsmurilo.inspection"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.smsmurilo.inspection"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { viewBinding = true }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.constraintlayout)
    implementation(libs.room.runtime)
    implementation(libs.room.rxjava3)
    annotationProcessor(libs.room.compiler)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    testImplementation(libs.junit)
    testImplementation(libs.mockito)
}
