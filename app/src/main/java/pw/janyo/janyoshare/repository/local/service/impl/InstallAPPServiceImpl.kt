package pw.janyo.janyoshare.repository.local.service.impl

import pw.janyo.janyoshare.model.InstallAPP
import pw.janyo.janyoshare.repository.local.db.DBHelper
import pw.janyo.janyoshare.repository.local.service.InstallAPPService

class InstallAPPServiceImpl : InstallAPPService {
	private val installAPPDao = DBHelper.db.getInstallAPPDao()

	override fun addInstallAPP(installAPP: InstallAPP): Long = installAPPDao.addInstallAPP(installAPP)

	override fun delInstallAPP(installAPP: InstallAPP): Int = installAPPDao.delInstallAPP(installAPP)

	override fun updateInstallAPP(installAPP: InstallAPP) = installAPPDao.updateInstallAPP(installAPP)

	override fun queryAPPForType(type: Int): List<InstallAPP> = installAPPDao.queryAPPForType(type)
}