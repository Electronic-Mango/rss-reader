package prm.project2.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ReadRssGuid::class], version = 1)
abstract class ReadRssGuidDatabase : RoomDatabase() {

    abstract fun readRssGuidDao(): ReadRssGuidDao
}