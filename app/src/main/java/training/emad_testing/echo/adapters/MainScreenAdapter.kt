package training.emad_testing.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import training.emad_testing.echo.R
import training.emad_testing.echo.Songs
import training.emad_testing.echo.fragments.SongPlayingFragment


class MainScreenAdapter(_songDetails: ArrayList<Songs>, _context: Context) :
    RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>() {

    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainScreenAdapter.MyViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null) {
            return 0
        } else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    override fun onBindViewHolder(holder: MainScreenAdapter.MyViewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.artist

        holder.contentHolder?.setOnClickListener {
            //            Toast.makeText(mContext , " Hey "+songObject?.songTitle , Toast.LENGTH_SHORT)
            val songPlayingFragment = SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", songObject?.artist)
            args.putString("path", songObject?.songData)
            args.putString("songTitle", songObject?.songTitle)
//            args.putInt("songId" , songObject?.songID?.toInt() as Int) //has been change to next line.
            // When reverting back changes look at SongPlayingFragment.kt
            //Search for "songId cant be converted to long"
            //also comment next line
            args.putLong("songId", songObject?.songID?.toLong() as Long)
            args.putInt("songPosition", position)
            args.putParcelableArrayList("songsData", songDetails)

            songPlayingFragment.arguments = args
            try {

                if (SongPlayingFragment.SongPlayingStatified.mediaplayer.isPlaying) {
                    SongPlayingFragment.SongPlayingStatified.mediaplayer.stop()
                }


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
            }

        }
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            trackTitle = view.findViewById<TextView>(R.id.trackTitle) as TextView
            trackArtist = view.findViewById<TextView>(R.id.trackArtist) as TextView
            contentHolder = view.findViewById<RelativeLayout>(R.id.contentRow) as RelativeLayout
        }

    }

}
