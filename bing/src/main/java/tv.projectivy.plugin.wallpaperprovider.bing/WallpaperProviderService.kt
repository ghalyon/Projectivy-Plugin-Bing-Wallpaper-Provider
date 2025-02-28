package tv.projectivy.plugin.wallpaperprovider.bing

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date

class WallpaperProviderService: Service() {

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    fun getCurrentDate():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }

    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> {
            var today = getCurrentDate()
            var plugindate = PreferencesManager.imageUrl.substringAfter("http://bing.biturl.top/?resolution=UHD&format=image&index=0&mkt=en-US?")
            if(today != plugindate){
                PreferencesManager.imageUrl = "http://bing.biturl.top/?resolution=UHD&format=image&index=0&mkt=en-US?$today"
            }

            return when (event) {
                is Event.TimeElapsed -> {
                    // This is where you generate the wallpaper list that will be cycled every x minute
                    return listOf(
                        // DRAWABLE can be served from app drawable/
                        //Wallpaper(getDrawableUri(R.drawable.ic_banner_drawable).toString(), WallpaperType.DRAWABLE),
                        // IMAGE can be served from app drawable/, local storage or internet
                        Wallpaper(PreferencesManager.imageUrl, WallpaperType.IMAGE, author = "Bing"),
                        // ANIMATED_DRAWABLE can be served from app drawable/
                        //Wallpaper(getDrawableUri(R.drawable.anim_sample).toString(), WallpaperType.ANIMATED_DRAWABLE),
                        // LOTTIE can be served from app raw/, local storage or internet
                        //Wallpaper(getDrawableUri(R.raw.gradient).toString(), WallpaperType.LOTTIE),
                        // VIDEO can be served from app raw/, local storage or internet (some formats might not be supported, though)
                        //Wallpaper(getDrawableUri(R.raw.light).toString(), WallpaperType.VIDEO)
                    )
                }

                // Below are "dynamic events" that might interest you in special cases
                // You will only receive dynamic events depending on the updateMode declared in your manifest
                // Don't subscribe if not interested :
                //  - this will consume device resources unnecessarily
                //  - some cache optimizations won't be enabled for dynamic wallpaper providers

                // When "now playing" changes (ex: a song starts or stops)
                is Event.NowPlayingChanged -> emptyList()
                // When the focused card changes
                is Event.CardFocused -> emptyList()
                // When the focused "program" card changes
                is Event.ProgramCardFocused -> emptyList()
                // When Projectivy enters or exits idle mode
                is Event.LauncherIdleModeChanged -> {
                    return if (event.isIdle) { return listOf(Wallpaper(PreferencesManager.imageUrl, WallpaperType.IMAGE, author = "Bing")) }
                        else  { emptyList() }
                }
                else -> emptyList()  // Returning an empty list won't change the currently displayed wallpaper
            }
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }

        fun getDrawableUri(drawableId: Int): Uri {
            return Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(drawableId))
                .appendPath(resources.getResourceTypeName(drawableId))
                .appendPath(resources.getResourceEntryName(drawableId))
                .build()
        }


    }
}