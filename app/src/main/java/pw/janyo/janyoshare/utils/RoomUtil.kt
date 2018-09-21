package pw.janyo.janyoshare.utils

import androidx.room.TypeConverter

object RoomUtil {
	@JvmStatic
	@TypeConverter
	fun convertArrayToString(array: Array<String>): String {
		val stringBuilder = StringBuilder()
		array.forEach { stringBuilder.append(it).append(',') }
		return stringBuilder.toString()
	}

	@JvmStatic
	@TypeConverter
	fun convertStringToArray(arrayString: String): Array<String> = arrayString.split(',').toTypedArray()
}