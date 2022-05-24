package xxvidownloader.video.xxvi.downloader.videos.saver.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import xxvidownloader.video.xxvi.downloader.videos.saver.fragments.BrowseFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.fragments.UrlLinkFragment

class ViewPagerAdapter(fragmentManger : FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManger,lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
      return  when(position){
          0 -> {
             UrlLinkFragment()
          }

          1 -> {
              BrowseFragment()
          }

          else ->{
              Fragment()
          }
      }
    }
}