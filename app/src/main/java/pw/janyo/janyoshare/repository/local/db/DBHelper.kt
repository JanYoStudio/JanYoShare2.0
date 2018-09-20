package pw.janyo.janyoshare.repository.local.db

import android.content.Context
import androidx.room.Room

object DBHelper {
	private const val DATABASE_NAME = "db_janyo_share"
	lateinit var db: DB
		private set

	fun init(context: Context) {
		db = Room.databaseBuilder(context.applicationContext, DB::class.java, DATABASE_NAME)
				.build()
	}
}