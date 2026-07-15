# Keep Hilt
-keepclassmembers class * {
    @dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper *;
}

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
