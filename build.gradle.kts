<<<<<<< HEAD
// Top-level build file
plugins {
    id("com.android.application") version "8.2.0" apply false
=======
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.2" apply false
>>>>>>> 2cfdab441d0f865a9efbdae55c2613ef495acca5
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

<<<<<<< HEAD
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

=======
>>>>>>> 2cfdab441d0f865a9efbdae55c2613ef495acca5
