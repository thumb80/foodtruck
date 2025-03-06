plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.bor.foodtruck"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bor.foodtruck"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        setProperty("archivesBaseName", "foodtruck")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        resources {
            excludes.plus("/res/raw/*.json").plus("/res/raw/*.jpg")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:${rootProject.extra["androidx_core_version"]}")
    implementation("androidx.appcompat:appcompat:${rootProject.extra["androidx_appcompat_version"]}")
    implementation("com.google.android.material:material:${rootProject.extra["com_google_android_material_version"]}")
    implementation ("com.google.code.gson:gson:${rootProject.extra["com_google_code_gson_version"]}")
    implementation("io.insert-koin:koin-android:${rootProject.extra["io_insert_koin_version"]}")
    implementation("androidx.navigation:navigation-fragment-ktx:${rootProject.extra["androidx_navigation_version"]}")
    implementation("androidx.navigation:navigation-ui-ktx:${rootProject.extra["androidx_navigation_version"]}")
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")
    implementation("com.github.sundeepk:compact-calendar-view:${rootProject.extra["calendar_library_version"]}")
    implementation("commons-io:commons-io:${rootProject.extra["apache_commons_version"]}")
    implementation("androidx.bluetooth:bluetooth:${rootProject.extra["bluetooth_version"]}")
    annotationProcessor("androidx.room:room-compiler:${rootProject.extra["room_version"]}")
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")

    implementation(files("./libs/hoinprinterlib-release.aar"))
    implementation(files("./libs/hoinSdk-release.aar"))

    implementation(files("./libs/printer-2.1.0.aar"))
    implementation(files("./libs/library-1.4.0.aar"))

    testImplementation("junit:junit:${rootProject.extra["junit_version"]}")
    androidTestImplementation("androidx.test.ext:junit:${rootProject.extra["androidx_test_ext_version"]}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${rootProject.extra["androidx_test_espresso_version"]}")
}