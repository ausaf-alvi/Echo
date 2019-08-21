package training.emad_testing.echo

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class CustomMediaPlayer : MediaPlayer() {
    private var dataSourceCustom: String = ""

    override fun setDataSource(context: Context?, uri: Uri) {
        super.setDataSource(context, uri)
        dataSourceCustom = uri.path
    }

    fun setDataSource(activity: Activity?, uri: Uri) {
        setDataSource(activity as Context?, uri)
    }

    fun setDataSource(context: Context?, uri: String) {
        var uriCorrected: Uri = Uri.parse(uri)
        super.setDataSource(context, uriCorrected)
        dataSourceCustom = uriCorrected.path
    }

    fun setDataSource(activity: Activity?, uri: String) {
        var uriCorrected: Uri = Uri.parse(uri)
        setDataSource(activity as Context?, uriCorrected)
    }

    fun getDataSource(): String {
        return dataSourceCustom
    }

    fun setDataSource(parse: Uri?) {
        var string_URI: String? = parse.toString()
        setDataSource(string_URI)
    }
}