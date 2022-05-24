package xxvidownloader.video.xxvi.downloader.videos.saver.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.ActivityMainBinding
import xxvidownloader.video.xxvi.downloader.videos.saver.fragments.HowToUseDialogFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.fragments.NoInternetConnectionFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.utils.UtilitiesClass
import org.koin.android.BuildConfig


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    private lateinit var binding: ActivityMainBinding
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var toolBar: Toolbar

    var drawerLayout: DrawerLayout? = null

    private lateinit var utilitiesClass: UtilitiesClass

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        utilitiesClass = UtilitiesClass(this)
        val connected = utilitiesClass.isNetworkConnected(this)
        if (!connected){
            NoInternetConnectionFragment().show(supportFragmentManager,NoInternetConnectionFragment.TAG)
        }

        initView()
        initializeAds()
        setSupportActionBar(toolBar)
        this.title = "XXVI Video Downloader"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationDrawerActions()


        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: readPermissionGranted
                writePermissionGranted =
                    permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: writePermissionGranted
            }
        updateOrRequestPermission()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.videoPlayerFragment || destination.id == R.id.downloadsFragment || destination.id == R.id.privacyPolicyFragment) {
                binding.toolbar.visibility = View.GONE
            } else {
                binding.toolbar.visibility = View.VISIBLE
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.downloads -> {
                navController.navigate(R.id.downloadsFragment)
            }
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



    private fun initView() {
        toolBar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navController = Navigation.findNavController(this, R.id.fragment)
    }

    private fun navigationDrawerActions() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Check XXVI Video Downloader: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
                return true
            }

            R.id.howTo -> {
                val fm = supportFragmentManager
                HowToUseDialogFragment().show(fm, HowToUseDialogFragment.TAG)
                return true
            }

            R.id.privacyPolicy -> {
                if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout!!.closeDrawer(GravityCompat.START)
                }
                navController.navigate(R.id.privacyPolicyFragment)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun updateOrRequestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()


        if (!readPermissionGranted) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!writePermissionGranted) {
            permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }


    private fun initializeAds() {
        MobileAds.initialize(this){}
    }


}