package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import xxvidownloader.video.xxvi.downloader.videos.saver.adapters.ViewPagerAdapter
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentViewPagerBinding

class ViewPagerFragment : Fragment() {
    private lateinit var binding : FragmentViewPagerBinding
    private lateinit var viewPager2 : ViewPager2
    private lateinit var tabLayout: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
      binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_pager, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = binding.viewPager2
        tabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter((activity as AppCompatActivity).supportFragmentManager,lifecycle)
        viewPager2.isUserInputEnabled = false;
        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout,viewPager2){tab,position ->
            when(position){

                0->{
                    tab.text = "Link"
                    tab.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_link_icon)
                }

                1 -> {
                    tab.text = "Browse"
                    tab.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_browse_24)
                }

                
            }
        }.attach()
    }

}