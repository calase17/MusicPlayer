package com.kgeorge.myapp;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.kgeorge.myapp.MainActivity.musicFiles;



/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment implements AdapterToFragment {
    static MusicAdapter musicAdapter;
    RecyclerView recyclerView;
    FragmentToActivity fa;


    public SongsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        if (!(musicFiles.size() < 1)){
            musicAdapter = new MusicAdapter(getContext(), musicFiles,this );
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false));
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fa  = (FragmentToActivity) context;

    }

    @Override
    public void miniPlayer(int position) {
        fa.fragmentActivity(position);


    }
}

