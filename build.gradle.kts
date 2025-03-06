// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.23" apply false
}

buildscript {
    extra.apply {
        set("androidx_core_version", "1.13.1")
        set("androidx_appcompat_version", "1.7.0")
        set("com_google_android_material_version", "1.12.0")
        set("com_google_code_gson_version", "2.11.0")
        set("io_insert_koin_version", "3.5.6")
        set("androidx_navigation_version", "2.8.0")
        set("room_version", "2.6.1")
        set("calendar_library_version","3.0.0")
        set("junit_version", "4.13.2")
        set("androidx_test_ext_version", "1.2.1")
        set("androidx_test_espresso_version", "3.6.1")
        set("apache_commons_version","2.16.1")
        set("bluetooth_version","1.0.0-alpha02")
    }
}