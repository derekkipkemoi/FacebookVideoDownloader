package xxvidownloader.video.xxvi.downloader.videos.saver.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import xxvidownloader.video.xxvi.downloader.videos.saver.fragments.HowToUseBrowseFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.fragments.HowToUseUrlLinkFragment

class HowToUseViewPagerAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager,lifecycle){
    override fun getItemCount(): Int {
       return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HowToUseUrlLinkFragment()
            1 -> HowToUseBrowseFragment()
            else -> Fragment()
        }
    }
}