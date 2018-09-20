package pw.janyo.janyoshare.repository.local.dao

import androidx.room.*
import pw.janyo.janyoshare.model.InstallAPP

@Dao
interface InstallAPPDao {
	@Insert
	fun addInstallAPP(installAPP: InstallAPP): Long

	@Delete
	fun delInstallAPP(installAPP: InstallAPP): Int

	@Update
	fun updateInstallAPP(installAPP: InstallAPP)

	@Query("select * from tb_install_app where ia_type = :type")
	fun queryAPPForType(type: Int): List<InstallAPP>
}