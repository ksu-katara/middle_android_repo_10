package ru.yandex.buggyweatherapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object ImageLoader {
    
    private lateinit var appContext: Context
    
    private val imageCache = ConcurrentHashMap<String, Bitmap>()
    
    // ОШИБКА 9
    private val viewReferences = ConcurrentHashMap<String, WeakReference<ImageView>>()
    
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
    
    suspend fun loadImage(url: String): Bitmap? {
        return if (imageCache.containsKey(url)) {
            imageCache[url]
        } else {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val connection = URL(url).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()
                    
                    val inputStream = connection.getInputStream()
                    val decoded = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    
                    if (decoded != null) {
                        imageCache[url] = decoded
                    }
                    decoded
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun loadImageSync(url: String): Bitmap? {
        return if (imageCache.containsKey(url)) {
            imageCache[url]
        } else {
            try {
                val connection = URL(url).openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                
                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                
                if (bitmap != null) {
                    imageCache[url] = bitmap
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }
    
    fun loadInto(url: String, imageView: ImageView) {
        viewReferences[url] = WeakReference(imageView)
        
        if (imageCache.containsKey(url)) {
            imageView.setImageBitmap(imageCache[url])
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = loadImageSync(url)
                withContext(Dispatchers.Main) {
                    val targetView = viewReferences[url]?.get()
                    if (targetView != null && bitmap != null) {
                        targetView.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
}