import android.content.Context
import org.json.JSONObject
import java.io.IOException

object ConfigUtil {
    fun getBaseUrl(context: Context): String {
        return try {
            val inputStream = context.assets.open("config.json")
            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            jsonObject.getString("baseUrl")
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}
