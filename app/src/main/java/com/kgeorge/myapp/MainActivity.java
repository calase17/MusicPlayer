package com.kgeorge.myapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, FragmentToActivity{
    static ArrayList<MusicFiles> musicFiles;
    private static final int PERMISSIONS_COUNT = 2;
    private static final int REQUEST_PERMISSIONS = 12345;
    private static String PATH = "_data";
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private void initViewPager(){
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumsFragment(), "Albums");
        viewPagerAdapter.addFragments(new PlaylistFragment(),"Playlists");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    public static ArrayList<MusicFiles> getAllAudioInfo(Context context){
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] extraction =
                {MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                PATH,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID};

        Cursor cursor = context.getContentResolver().query(uri, extraction,null,null,null);

        if (cursor!=null){
            while (cursor.moveToNext()){
                int pathIndex = cursor.getColumnIndexOrThrow(PATH);
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(pathIndex);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);



                MusicFiles musicFiles = new MusicFiles(path,title,artist, album, duration, id);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }
        return tempAudioList;

    }

    private void miniPlayerInit(int position) {
        FragmentManager fragmentManager =  getSupportFragmentManager();
        MiniPlayerFragment mpF = new MiniPlayerFragment(position);
        fragmentManager.beginTransaction().replace(R.id.contMp, mpF,"miniplayer").commit();
    }

    @Override
    public void fragmentActivity(int position) {
        miniPlayerInit(position);
    }


    public static class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);

        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();

        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    private boolean permissionsDenied(){
        for(int i = 0; i< PERMISSIONS_COUNT; i++){
            if(checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int codeRequest, String[] permissions, int[] grantResult){
        super.onRequestPermissionsResult(codeRequest, permissions, grantResult);
        if(permissionsDenied()){
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        }
        else {
            onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        else{
            musicFiles = getAllAudioInfo(this);
            initViewPager();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.searchBar);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String input  = newText.toLowerCase();
        ArrayList<MusicFiles> returnedSongs = new ArrayList<>();
        for (MusicFiles song : musicFiles){
            if (song.getTitle().toLowerCase().contains(input)){
                returnedSongs.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(returnedSongs);

        return true;
    }


}

