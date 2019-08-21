package training.emad_testing.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import training.emad_testing.echo.CustomMediaPlayer
import training.emad_testing.echo.R
import training.emad_testing.echo.Songs
import training.emad_testing.echo.adapters.FavoriteAdapter
import training.emad_testing.echo.databases.EchoDatabase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class FavoriteFragment : Fragment() {


    var noFavorites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null
    var getListFromDatabase: ArrayList<Songs>? = null

    object Statified {
        var mediaPlayer: CustomMediaPlayer? = null
    }

    var myActivity: Activity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)
        activity?.title = "Favorites"
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        songTitle = view?.findViewById(R.id.songTitleFavScreen)
        recyclerView = view?.findViewById(R.id.favoriteRecycler) as RecyclerView

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(myActivity)
        display_favorites_by_searching()
        bottomBarSetup()


    }


    /*
    *Following Function has been copied fom MainScreenFragment.kt file
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }

        }
        return arrayList
    }

    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.text = SongPlayingFragment.SongPlayingStatified.currentSongHelper?.songTitle
            SongPlayingFragment.SongPlayingStatified.mediaplayer.setOnCompletionListener {
                songTitle?.text = SongPlayingFragment.SongPlayingStatified.currentSongHelper?.songTitle
                SongPlayingFragment.Staticated.onSongComplete()
            }
            if (SongPlayingFragment.SongPlayingStatified.mediaplayer.isPlaying) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {
        nowPlayingBottomBar?.setOnClickListener {
            Statified.mediaPlayer = SongPlayingFragment.SongPlayingStatified.mediaplayer
            var songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", SongPlayingFragment.SongPlayingStatified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.SongPlayingStatified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.SongPlayingStatified.currentSongHelper?.songTitle)
            args.putLong("songId", SongPlayingFragment.SongPlayingStatified.currentSongHelper?.songId?.toLong() as Long)
            args.putInt(
                "songPosition",
                SongPlayingFragment.SongPlayingStatified.currentSongHelper?.currentPosition?.toInt() as Int
            )
            args.putParcelableArrayList("songsData", SongPlayingFragment.SongPlayingStatified.fetchSongs)
            args.putString("FavBottomBar", "success")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, songPlayingFragment)
                ?.addToBackStack("SongPlayingFragment")
                ?.commit()
        }
        playPauseButton?.setOnClickListener {
            if (SongPlayingFragment.SongPlayingStatified.mediaplayer.isPlaying) {
                SongPlayingFragment.SongPlayingStatified.mediaplayer.pause()
                trackPosition = SongPlayingFragment.SongPlayingStatified.mediaplayer.currentPosition
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                SongPlayingFragment.SongPlayingStatified.mediaplayer.seekTo(trackPosition)
                SongPlayingFragment.SongPlayingStatified.mediaplayer.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun display_favorites_by_searching() {
        Log.d("display_favorites_...", "Function is called")
        if (favoriteContent?.checkSize() as Int > 0) {
//            getListFromDatabase = ArrayList<Songs>()
            refreshList = ArrayList<Songs>()
            getListFromDatabase = favoriteContent?.queryDBList()
            var fetchListFromDevice = getSongsFromPhone()
            if (fetchListFromDevice != null) {
                for (i in 0..fetchListFromDevice.size - 1) {
                    for (j in 0..getListFromDatabase?.size as Int - 1) {
                        if (fetchListFromDevice.get(i).songID === getListFromDatabase?.get(j)?.songID) {
                            refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                            var trackInformation = getListFromDatabase?.get(j)?.songData.toString()
                            var lastForwardSlash = trackInformation.lastIndexOf('/')
                            var lastDot = trackInformation.lastIndexOf('.')
                            var fileName = trackInformation.substring(lastForwardSlash + 1, lastDot).trim()
                            Log.d("Song match in favorites", "Name of song: " + fileName)

                        }
                    }
                }
            } else {
                Log.d("Favorite List", "fetchListFromDevice is null")
            }
            if (refreshList == null) {
                Log.d("Favorite List", "refreshList is null")
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            } else {
                Log.d("Favorite List", "List is not empty. It is not getting updated.")
                var favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        } else {
            Log.d("Favorite List", "favoriteContent.size <= 0")
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }
}


