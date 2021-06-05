package prm.project2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReadRssGuidDao {

    @Query("SELECT * FROM ReadRssGuid")
    fun getAll(): List<ReadRssGuid>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(guid: ReadRssGuid): Long

    @Query("DELETE FROM ReadRssGuid")
    fun deleteAll()

}