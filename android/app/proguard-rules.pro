# Play Billing, the Mobile Ads SDK and UMP ship their own consumer rules.
# The ads SDK pulls in WorkManager, whose Room database is created by reflection; Room's consumer
# rules omit the no-argument constructor R8 full mode strips, and the app then dies at startup in
# androidx.startup.InitializationProvider ("Failed to create an instance of androidx.work.impl.WorkDatabase").
-keep class * extends androidx.room.RoomDatabase {
    void <init>();
}
