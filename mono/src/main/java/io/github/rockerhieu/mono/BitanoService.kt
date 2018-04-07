package io.github.rockerhieu.mono

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class BitanoService {
    interface Callback<in T> {
        fun onSuccess(result: T?)
        fun onError(e: Exception)
    }

    companion object {
        private const val TAG = "BitanoService"
        private val client = OkHttpClient()

        private suspend fun get(url: String): String {
            Log.d(TAG, "GET Request $url")
            val response = client.newCall(Request.Builder().get()
                    .url(url)
                    .build()).execute()
            val result = response.body()?.string() ?: ""
            Log.d(TAG, "GET Response: $result")
            return result
        }

        suspend fun updateTemperature(value: Double) {
            get("http://api.thingspeak.com/update?api_key=B5OCC6KIKKJTWOO1&field1="
                    + String.format("%.2f", value))
        }

        fun updateTemperatureAsync(value: Double, callback: Callback<Unit>? = null) {
            async(CommonPool) {
                try {
                    updateTemperature(value)
                    callback?.onSuccess(null)
                } catch (e: Exception) {
                    callback?.onError(e)
                }
            }
        }

        suspend fun annotateImage(imageUrl: String): String {
            return get("https://us-central1-mono-f05a6.cloudfunctions.net/http?uri=" +
                    Uri.encode(imageUrl))
        }

        fun annotateImageAsync(imageUrl: String, callback: Callback<List<String>>? = null) {
            async(CommonPool) {
                try {
                    val labels = JSONObject(annotateImage(imageUrl)).getJSONArray("labels")
                    val result = ArrayList<String>(labels.length())
                    (0 until labels.length()).map { labels.getString(it) }.mapTo(result) { it }
                    callback?.onSuccess(result)
                } catch (e: Exception) {
                    callback?.onError(e)
                }
            }
        }
    }
}