package pw.janyo.janyoshare.repository.local.service

import pw.janyo.janyoshare.model.InstallAPP

interface InstallAPPService {
	fun addInstallAPP(installAPP: InstallAPP): Long

	fun delInstallAPP(installAPP: InstallAPP): Int

	fun updateInstallAPP(installAPP: InstallAPP)

	fun queryAPPForType(type: Int): List<InstallAPP>
}