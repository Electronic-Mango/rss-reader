package prm.project2.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReadRssGuid(
    @PrimaryKey val guid: String
)