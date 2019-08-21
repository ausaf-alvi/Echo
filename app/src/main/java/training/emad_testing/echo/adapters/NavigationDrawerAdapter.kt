package training.emad_testing.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import training.emad_testing.echo.R
import training.emad_testing.echo.activities.MainActivity
import training.emad_testing.echo.fragments.AboutUsFragment
import training.emad_testing.echo.fragments.FavoriteFragment
import training.emad_testing.echo.fragments.MainScreenFragment
import training.emad_testing.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context: Context) :
    RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {
    var contentList: ArrayList<String>? = null
    var getImages: IntArray? = null
    var mContext: Context? = null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun getItemCount(): Int {
        //Here we return the size of the list we used
        return (contentList as ArrayList).size
        //Note: Changed From  return (contentList as ArrayList).size as Int
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder.text_GET?.text = contentList?.get(position)
        holder.contentHolder?.setOnClickListener({
            if (position == 0) {
                //All Songs
                val mainScreenFragment = MainScreenFragment()
                //Now change current fragment to all songs fragment
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, mainScreenFragment)
                    .commit()
            } else if (position == 1) {
                //Favorite Songs
                val favoriteFragment = FavoriteFragment()
                //Now change current fragment to favorite fragment
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, favoriteFragment)
                    .commit()
            } else if (position == 2) {
                //Settings
                val settingsFragment = SettingsFragment()
                //Now change current fragment to Settings fragment
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, settingsFragment)
                    .commit()
            } else {
                //About Us
                val aboutUsFragment = AboutUsFragment()
                //Now change current fragment to favorite fragment
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, aboutUsFragment)
                    .commit()
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        var itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_navigationdrawer, parent, false)
        val returnThis = NavViewHolder(itemView)
        return returnThis
    }

    class NavViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var icon_GET: ImageView? = null
        var text_GET: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            icon_GET = itemView?.findViewById(R.id.icon_navdrawer) as ImageView
            text_GET = itemView.findViewById(R.id.text_navdrawer) as TextView
            contentHolder = itemView.findViewById(R.id.navdrawer_item_content_holder) as RelativeLayout
        }
    }
}