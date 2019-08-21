package training.emad_testing.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.cleveroad.audiovisualization.VisualizerDbmHandler
import training.emad_testing.echo.CurrentSongHelper
import training.emad_testing.echo.CustomMediaPlayer
import training.emad_testing.echo.R
import training.emad_testing.echo.Songs
import training.emad_testing.echo.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit






class SongPlayingFragment : Fragment() {

    object SongPlayingStatified {
        var mediaplayer: CustomMediaPlayer = CustomMediaPlayer()
        var currentSongHelper: CurrentSongHelper? = null
        var fetchSongs: ArrayList<Songs>? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var currentPosition: Int = 0
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var fab: ImageButton? = null
        var myActivity: Activity? = null
        var MY_PREFS_NAME = "ShakeFeature"
        var favoriteContent: EchoDatabase? = null

        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var updateSongTime = object : Runnable {
            override fun run() {

                val getCurrent = SongPlayingStatified.mediaplayer.currentPosition
                SongPlayingStatified.startTimeText?.text = String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(getCurrent.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong()))
                )
                SongPlayingStatified.seekbar?.progress = getCurrent.toInt()
                Handler().postDelayed(this, 1000)
            }
        }

    }

    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle feature"
        var MY_PREFS_LOOP = "Loop feature"

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            SongPlayingStatified.seekbar?.max = finalTime
            var startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())
            )
            var finalTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())
            )
            SongPlayingStatified.startTimeText?.text = String.format(
                "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                startTimeInSeconds
            )
            SongPlayingStatified.endTimeText?.text = String.format(
                "%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                finalTimeInSeconds
            )
            SongPlayingStatified.seekbar?.progress = startTime
            Handler().postDelayed(SongPlayingStatified.updateSongTime, 1000)
        }

        fun clickHandler() {
            SongPlayingStatified.fab?.setOnClickListener {
                if (SongPlayingStatified.favoriteContent?.checkIfIdExists(SongPlayingStatified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                    SongPlayingStatified.fab?.setImageDrawable(
                        ContextCompat.getDrawable(
                            SongPlayingStatified.myActivity as Context,
                            R.drawable.favorite_off
                        )
                    )
                    SongPlayingStatified.favoriteContent?.deleteFavorite(SongPlayingStatified.currentSongHelper?.songId?.toInt() as Int)
                    Toast.makeText(SongPlayingStatified.myActivity, "Removed From Favorites", Toast.LENGTH_SHORT)
                } else {
                    SongPlayingStatified.fab?.setImageDrawable(
                        ContextCompat.getDrawable(
                            SongPlayingStatified.myActivity as Context,
                            R.drawable.favorite_on
                        )
                    )
                    SongPlayingStatified.favoriteContent?.storeAsFavorite(
                        SongPlayingStatified.currentSongHelper?.songId?.toInt(),
                        SongPlayingStatified.currentSongHelper?.songArtist,
                        SongPlayingStatified.currentSongHelper?.songTitle,
                        SongPlayingStatified.currentSongHelper?.songPath
                    )
                    Toast.makeText(SongPlayingStatified.myActivity, "Added to Favorites!!", Toast.LENGTH_SHORT)
                }
            }
            SongPlayingStatified.shuffleImageButton?.setOnClickListener {
                var editorShuffle = SongPlayingStatified.myActivity?.getSharedPreferences(
                    Staticated.MY_PREFS_SHUFFLE,
                    Context.MODE_PRIVATE
                )?.edit()
                var editorLoop = SongPlayingStatified.myActivity?.getSharedPreferences(
                    Staticated.MY_PREFS_LOOP,
                    Context.MODE_PRIVATE
                )?.edit()
                if (SongPlayingStatified.currentSongHelper?.isShuffle as Boolean) {
                    SongPlayingStatified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                    SongPlayingStatified.currentSongHelper?.isShuffle = false
                    editorShuffle?.putBoolean("feature", false)
                    editorShuffle?.apply()
                } else {
                    SongPlayingStatified.currentSongHelper?.isShuffle = true
                    SongPlayingStatified.currentSongHelper?.isLoop = false
                    SongPlayingStatified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                    SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                    editorShuffle?.putBoolean("feature", true)
                    editorShuffle?.apply()
                    editorLoop?.putBoolean("feature", false)
                    editorLoop?.apply()
                }
            }
            SongPlayingStatified.nextImageButton?.setOnClickListener {
                SongPlayingStatified.currentSongHelper?.isPlaying = true
                SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                if (SongPlayingStatified.currentSongHelper?.isShuffle as Boolean) {
                    Staticated.playNext("PlayNextShuffle")
                } else {
                    Staticated.playNext("PlayNextNormal")
                }
            }
            SongPlayingStatified.previousImageButton?.setOnClickListener {
                SongPlayingStatified.currentSongHelper?.isPlaying = true
                if (SongPlayingStatified.currentSongHelper?.isLoop as Boolean) {
                    SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                }
                Staticated.playPrevious()
            }

            SongPlayingStatified.loopImageButton?.setOnClickListener {
                var editorShuffle = SongPlayingStatified.myActivity?.getSharedPreferences(
                    Staticated.MY_PREFS_SHUFFLE,
                    Context.MODE_PRIVATE
                )?.edit()
                var editorLoop = SongPlayingStatified.myActivity?.getSharedPreferences(
                    Staticated.MY_PREFS_LOOP,
                    Context.MODE_PRIVATE
                )?.edit()
                if (SongPlayingStatified.currentSongHelper?.isLoop as Boolean) {
                    SongPlayingStatified.currentSongHelper?.isLoop = false
                    SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                    editorLoop?.putBoolean("feature", false)
                    editorLoop?.apply()
                } else {
                    SongPlayingStatified.currentSongHelper?.isLoop = true
                    SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                    SongPlayingStatified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                    editorShuffle?.putBoolean("feature", false)
                    editorShuffle?.apply()
                    editorLoop?.putBoolean("feature", true)
                    editorLoop?.apply()
                }
            }
            SongPlayingStatified.playpauseImageButton?.setOnClickListener {
                if (SongPlayingStatified.mediaplayer.isPlaying) {
                    SongPlayingStatified.mediaplayer.pause()
                    SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                    SongPlayingStatified.currentSongHelper?.isPlaying = false
                } else {
                    SongPlayingStatified.mediaplayer.start()
                    SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                    SongPlayingStatified.currentSongHelper?.isPlaying = true
                }
            }
            SongPlayingStatified.seekbar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        SongPlayingStatified.mediaplayer?.seekTo(progress)
                        Staticated.processInformation(SongPlayingStatified.mediaplayer)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })
        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                SongPlayingStatified.currentPosition = SongPlayingStatified.currentPosition.plus(1)
                //Log.d(" play next ",("NORMAL : current position changed"+SongPlayingStatified.currentPosition.toString()))
            } else if (check.equals("PlayNextShuffle", true)) {
                var randomObject = Random()
                var randomPosition = 0
                if (SongPlayingStatified.fetchSongs != null) randomPosition =
                    randomObject.nextInt(SongPlayingStatified.fetchSongs?.size?.plus(1) as Int)
                SongPlayingStatified.currentPosition = randomPosition
                //Log.d(" play next ",("SHUFFLE : current position changed"+SongPlayingStatified.currentPosition.toString()))
            }
            if ((SongPlayingStatified.fetchSongs != null) && (SongPlayingStatified.currentPosition == SongPlayingStatified.fetchSongs?.size || SongPlayingStatified.currentPosition > SongPlayingStatified.fetchSongs!!.size)) {
                SongPlayingStatified.currentPosition = 0
                //Log.d(" play next ",("LAST SONG : current position changed"+SongPlayingStatified.currentPosition.toString()))
            }
            SongPlayingStatified.currentSongHelper?.isLoop = false
            var nextSong = SongPlayingStatified.fetchSongs?.get(SongPlayingStatified.currentPosition.toString().toInt())
            SongPlayingStatified.currentSongHelper?.songPath = nextSong?.songData
            SongPlayingStatified.currentSongHelper?.songTitle = nextSong?.songTitle
            SongPlayingStatified.currentSongHelper?.songArtist = nextSong?.artist
            if (nextSong?.songID != null) SongPlayingStatified.currentSongHelper?.songId = nextSong.songID
            SongPlayingStatified.currentSongHelper?.currentPosition = SongPlayingStatified.currentPosition
            if (SongPlayingStatified.currentSongHelper?.songTitle == null) {
                SongPlayingStatified.currentSongHelper?.songTitle = "Unknown"
                //Log.d("check 1","SongTitle was Null at Staticated.playNext")
            }
            if (SongPlayingStatified.currentSongHelper?.songArtist == null) {
                SongPlayingStatified.currentSongHelper?.songArtist = "Unknown"
                //Log.d("check 2","SongArtist was Null at Staticated.playNext")
            }
            Staticated.updateTextViews(
                SongPlayingStatified.currentSongHelper?.songTitle as String,
                SongPlayingStatified.currentSongHelper?.songArtist as String
            )
            try {

                //Log.d(" play next Null path ", ("SongPath: "+SongPlayingStatified.currentSongHelper?.songPath+" Song Name: "+SongPlayingStatified.mediaplayer?.getDataSource()))
                if (SongPlayingStatified.currentSongHelper?.songPath.toString().compareTo("null", true) == 0) {
                    //Log.d("Song Path Value: ",SongPlayingStatified.currentSongHelper?.songPath.toString())
                    SongPlayingStatified.mediaplayer.pause()
                    SongPlayingStatified.currentSongHelper?.isPlaying = false
                    SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                    //Log.d("Current Position", "Current: "+SongPlayingStatified.currentPosition)
                }
                Staticated.reInitializeMusicPlayer()
                SongPlayingStatified.mediaplayer.setDataSource(
                    SongPlayingStatified.myActivity as Context?,
                    Uri.parse(SongPlayingStatified.currentSongHelper?.songPath as String)
                )
                SongPlayingStatified.mediaplayer.prepare()
                SongPlayingStatified.currentSongHelper?.isPlaying = false
                SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                SongPlayingStatified.mediaplayer.start()
                Staticated.processInformation(SongPlayingStatified.mediaplayer as MediaPlayer)
                SongPlayingStatified.mediaplayer.setOnCompletionListener {
                    Staticated.onSongComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (SongPlayingStatified.favoriteContent?.checkIfIdExists(SongPlayingStatified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                SongPlayingStatified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        SongPlayingStatified.myActivity as Context,
                        R.drawable.favorite_on
                    )
                )
            } else {
                SongPlayingStatified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        SongPlayingStatified.myActivity as Context,
                        R.drawable.favorite_off
                    )
                )
            }
        }

        fun updateTextViews(songTitle: String, songArtist: String) {
            var st: String = "SongsArtist: " + songArtist + ".....SongTitle: " + songTitle
            //Log.d("check updateTextView", st)
            if (songTitle.compareTo("Unknown", true) == 0) {
                var trackInformation = SongPlayingStatified.mediaplayer.getDataSource().toString()
                var lastForwardSlash = trackInformation.lastIndexOf('/')
                var lastDot = trackInformation.lastIndexOf('.')
                var fileName = trackInformation.substring(lastForwardSlash + 1, lastDot).trim()
                //Log.d("Media File Info", fileName)
                SongPlayingStatified.songTitleView?.text = fileName
            } else {
                SongPlayingStatified.songTitleView?.text = songTitle
            }
            SongPlayingStatified.songArtistView?.text = songArtist
        }

        fun reInitializeMusicPlayer() {
            SongPlayingStatified.mediaplayer.reset()
            SongPlayingStatified.mediaplayer = CustomMediaPlayer()
            SongPlayingStatified.mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            SongPlayingStatified.mediaplayer.apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            var visualizationHandler: VisualizerDbmHandler =
                DbmHandler.Factory.newVisualizerHandler(SongPlayingStatified.mediaplayer)
            SongPlayingStatified.audioVisualization?.linkTo(visualizationHandler)
        }

        fun onSongComplete() {
            //Log.d(" Song Completed ", "isShuffle: "+SongPlayingStatified.currentSongHelper?.isShuffle
            //+"  \nisLoop: "+SongPlayingStatified.currentSongHelper?.isLoop
            //+"  \nisPlaying: "+SongPlayingStatified.currentSongHelper?.isPlaying)
            if (SongPlayingStatified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextShuffle")
                SongPlayingStatified.currentSongHelper?.isPlaying = true
                SongPlayingStatified.mediaplayer.start()
            } else {
                if (SongPlayingStatified.currentSongHelper?.isLoop as Boolean) {
                    //That is......Repeat the same song
                    SongPlayingStatified.currentSongHelper?.isPlaying = true
                    var nextSong = SongPlayingStatified.fetchSongs?.get(SongPlayingStatified.currentPosition)
                    SongPlayingStatified.currentSongHelper?.songTitle = nextSong?.songTitle
                    SongPlayingStatified.currentSongHelper?.songPath = nextSong?.songData
                    if (nextSong?.songID != null) SongPlayingStatified.currentSongHelper?.songId =
                        nextSong.songID
                    SongPlayingStatified.currentSongHelper?.currentPosition = SongPlayingStatified.currentPosition
                    if (SongPlayingStatified.currentSongHelper?.songTitle == null) {
                        SongPlayingStatified.currentSongHelper?.songTitle = "Unknown"
                        //Log.d("check 1","SongTitle was Null at song complete")
                    }
                    if (SongPlayingStatified.currentSongHelper?.songArtist == null) {
                        SongPlayingStatified.currentSongHelper?.songArtist = "Unknown"
                        //Log.d("check 2","SongArtist was Null at song complete")
                    }
                    Staticated.updateTextViews(
                        SongPlayingStatified.currentSongHelper?.songTitle as String,
                        SongPlayingStatified.currentSongHelper?.songArtist as String
                    )
                    try {
                        SongPlayingStatified.mediaplayer.start()
                        SongPlayingStatified.currentSongHelper?.isLoop = true
                        SongPlayingStatified.mediaplayer.setOnCompletionListener {
                            Staticated.onSongComplete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Staticated.playNext("PlayNextNormal")
                    SongPlayingStatified.currentSongHelper?.isPlaying = true
                }
            }
            if (SongPlayingStatified.favoriteContent?.checkIfIdExists(SongPlayingStatified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                SongPlayingStatified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        SongPlayingStatified.myActivity as Context,
                        R.drawable.favorite_on
                    )
                )
            } else {
                SongPlayingStatified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        SongPlayingStatified.myActivity as Context,
                        R.drawable.favorite_off
                    )
                )
            }

        }
        fun seekbarChanged(){

        }

        fun playPrevious() {
            SongPlayingStatified.currentPosition = SongPlayingStatified.currentPosition - 1
            if (SongPlayingStatified.currentPosition == -1) {
                SongPlayingStatified.currentPosition = 0
            }
            if (SongPlayingStatified.currentSongHelper?.isPlaying as Boolean) {
                SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }
            SongPlayingStatified.currentSongHelper?.isLoop = false
            var nextSong = SongPlayingStatified.fetchSongs?.get(SongPlayingStatified.currentPosition)
            SongPlayingStatified.currentSongHelper?.songPath = nextSong?.songData
            SongPlayingStatified.currentSongHelper?.songTitle = nextSong?.songTitle
            SongPlayingStatified.currentSongHelper?.songArtist = nextSong?.artist
            if (nextSong?.songID != null) SongPlayingStatified.currentSongHelper?.songId = nextSong.songID
            SongPlayingStatified.currentSongHelper?.currentPosition = SongPlayingStatified.currentPosition
            if (SongPlayingStatified.currentSongHelper?.songTitle == null) {
                SongPlayingStatified.currentSongHelper?.songTitle = "Unknown"
                //Log.d("check 1","SongTitle was Null at Staticated.playPrevious")
            }
            if (SongPlayingStatified.currentSongHelper?.songArtist == null) {
                SongPlayingStatified.currentSongHelper?.songArtist = "Unknown"
                //Log.d("check 2","SongArtist was Null at Staticated.playPrevious")
            }
            Staticated.updateTextViews(
                SongPlayingStatified.currentSongHelper?.songTitle as String,
                SongPlayingStatified.currentSongHelper?.songArtist as String
            )
            try {
                Staticated.reInitializeMusicPlayer()
                SongPlayingStatified.mediaplayer.apply {
                    setDataSource(
                        SongPlayingStatified.myActivity,
                        Uri.parse(SongPlayingStatified.currentSongHelper?.songPath as String)
                    )
                }
                SongPlayingStatified.mediaplayer.prepare()
                SongPlayingStatified.mediaplayer.start()
                Staticated.processInformation(SongPlayingStatified.mediaplayer as MediaPlayer)
                SongPlayingStatified.mediaplayer.setOnCompletionListener {
                    Staticated.onSongComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (SongPlayingStatified.favoriteContent?.checkIfIdExists(SongPlayingStatified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                SongPlayingStatified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        SongPlayingStatified.myActivity as Context,
                        R.drawable.favorite_on
                    )
                )
            } else {
                SongPlayingStatified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        SongPlayingStatified.myActivity as Context,
                        R.drawable.favorite_off
                    )
                )
            }
        }
//        Here Ends Staticated object
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        SongPlayingStatified.startTimeText = view?.findViewById<TextView>(R.id.startTime) as TextView
        SongPlayingStatified.endTimeText = view.findViewById<TextView>(R.id.endTime) as TextView
        SongPlayingStatified.songArtistView = view.findViewById<TextView>(R.id.songArtist) as TextView
        SongPlayingStatified.songTitleView = view.findViewById<TextView>(R.id.songTitle) as TextView
        SongPlayingStatified.playpauseImageButton = view.findViewById<ImageButton>(R.id.playPauseButton) as ImageButton
        SongPlayingStatified.nextImageButton = view.findViewById<ImageButton>(R.id.nextButton) as ImageButton
        SongPlayingStatified.previousImageButton = view.findViewById<ImageButton>(R.id.previousButton) as ImageButton
        SongPlayingStatified.loopImageButton = view.findViewById<ImageButton>(R.id.loopButton) as ImageButton
        SongPlayingStatified.shuffleImageButton = view.findViewById<ImageButton>(R.id.shuffleButton) as ImageButton
        SongPlayingStatified.seekbar = view.findViewById<SeekBar>(R.id.seekBar) as SeekBar
        SongPlayingStatified.glView = view.findViewById(R.id.visualizer_view) as GLAudioVisualizationView
        SongPlayingStatified.fab = view.findViewById(R.id.favoriteIcon) as ImageButton
        SongPlayingStatified.fab?.alpha = 0.8f //To dim the heart icon
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        SongPlayingStatified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        SongPlayingStatified.myActivity = activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SongPlayingStatified.audioVisualization = SongPlayingStatified.glView as AudioVisualization
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        SongPlayingStatified.favoriteContent = EchoDatabase(SongPlayingStatified.myActivity)
        SongPlayingStatified.currentSongHelper = CurrentSongHelper()
        SongPlayingStatified.currentSongHelper?.isPlaying = true
        SongPlayingStatified.currentSongHelper?.isShuffle = false
        SongPlayingStatified.currentSongHelper?.isLoop = false
        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle") as String
            _songArtist = arguments?.getString("songArtist") as String
//            songId = arguments?.getInt("songId") as Long //Has Been changed to next Line
//            songId cant be converted to long
//            (Uncomment this and make next line as commented)
            songId = arguments?.getLong("songId") as Long
            SongPlayingStatified.currentPosition = arguments?.getInt("songPosition") as Int
            SongPlayingStatified.fetchSongs = arguments?.getParcelableArrayList("songsData")
            SongPlayingStatified.currentSongHelper?.songPath = path
            SongPlayingStatified.currentSongHelper?.songTitle = _songTitle
            SongPlayingStatified.currentSongHelper?.songArtist = _songArtist
            SongPlayingStatified.currentSongHelper?.songId = songId
            SongPlayingStatified.currentSongHelper?.currentPosition = SongPlayingStatified.currentPosition
            if (SongPlayingStatified.currentSongHelper?.songTitle == null) {
                SongPlayingStatified.currentSongHelper?.songTitle = "Unknown"
                //Log.d("check 1","SongTitle was Null at onActivityCreated")
            }
            if (SongPlayingStatified.currentSongHelper?.songArtist == null) {
                SongPlayingStatified.currentSongHelper?.songArtist = "Unknown"
                //Log.d("check 2","SongArtist was Null at onActivityCreated")
            }
            Staticated.updateTextViews(
                SongPlayingStatified.currentSongHelper?.songTitle as String,
                SongPlayingStatified.currentSongHelper?.songArtist as String
            )
            //Log.d("check 3","song artist and song title both not null")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Staticated.clickHandler()

        var fromFavBottom = arguments?.get("FavBottomBar") as? String
        if (fromFavBottom != null) {
            SongPlayingStatified.mediaplayer = FavoriteFragment.Statified.mediaPlayer!!
        } else {
            SongPlayingStatified.mediaplayer = CustomMediaPlayer()
            SongPlayingStatified.mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            SongPlayingStatified.mediaplayer.apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            try {
//            SongPlayingStatified.mediaplayer?.stop()
                SongPlayingStatified.mediaplayer.apply {
                    setDataSource(
                        SongPlayingStatified.myActivity,
                        Uri.parse(path)
                    )
                }
                SongPlayingStatified.mediaplayer.prepare()
                SongPlayingStatified.currentSongHelper?.isPlaying = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            SongPlayingStatified.mediaplayer.start()
        }

        Staticated.processInformation(SongPlayingStatified.mediaplayer as MediaPlayer)
        if (SongPlayingStatified.currentSongHelper?.isPlaying as Boolean) {
            SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            SongPlayingStatified.playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        SongPlayingStatified.mediaplayer.setOnCompletionListener {
            Staticated.onSongComplete()
        }
//        var visualizationHandler: VisualizerDbmHandler = DbmHandler.Factory.newVisualizerHandler(SongPlayingStatified.myActivity as Context, 0)
        var visualizationHandler: VisualizerDbmHandler =
            DbmHandler.Factory.newVisualizerHandler(SongPlayingStatified.mediaplayer)
        SongPlayingStatified.audioVisualization?.linkTo(visualizationHandler)
        var prefsForShuffle =
            SongPlayingStatified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            SongPlayingStatified.currentSongHelper?.isShuffle = true
            SongPlayingStatified.currentSongHelper?.isLoop = false
            SongPlayingStatified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            SongPlayingStatified.currentSongHelper?.isShuffle = false
            SongPlayingStatified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var prefsForLoop =
            SongPlayingStatified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            SongPlayingStatified.currentSongHelper?.isShuffle = false
            SongPlayingStatified.currentSongHelper?.isLoop = true
            SongPlayingStatified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            SongPlayingStatified.currentSongHelper?.isLoop = false
            SongPlayingStatified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        if (SongPlayingStatified.favoriteContent?.checkIfIdExists(SongPlayingStatified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            SongPlayingStatified.fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    SongPlayingStatified.myActivity as Context,
                    R.drawable.favorite_on
                )
            )
        } else {
            SongPlayingStatified.fab?.setImageDrawable(
                ContextCompat.getDrawable(
                    SongPlayingStatified.myActivity as Context,
                    R.drawable.favorite_off
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        SongPlayingStatified.audioVisualization?.onResume()
        SongPlayingStatified.mSensorManager?.registerListener(
            SongPlayingStatified.mSensorListener,
            SongPlayingStatified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        SongPlayingStatified.audioVisualization?.onPause()
        super.onPause()

        SongPlayingStatified.mSensorManager?.unregisterListener(SongPlayingStatified.mSensorListener)
    }

    override fun onDestroyView() {
        SongPlayingStatified.audioVisualization?.release()
        super.onDestroyView()
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SongPlayingStatified.mSensorManager =
            SongPlayingStatified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }


    fun bindShakeListener() {
        SongPlayingStatified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x) + (y * y) + (z + z)).toDouble()).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12) {
                    val prefs = SongPlayingStatified.myActivity?.getSharedPreferences(
                        SongPlayingStatified.MY_PREFS_NAME,
                        Context.MODE_PRIVATE
                    )
                    var isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                SongPlayingStatified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }


}
