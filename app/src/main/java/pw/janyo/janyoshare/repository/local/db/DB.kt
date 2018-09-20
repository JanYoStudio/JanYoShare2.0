package pw.janyo.janyoshare.repository.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.repository.local.dao.InstallAPPDao

@Database(entities = [InstallAPP::class], version = 1)
abstract class DB : RoomDatabase() {
	abstract fun getInstallAPPDao(): InstallAPPDao
}