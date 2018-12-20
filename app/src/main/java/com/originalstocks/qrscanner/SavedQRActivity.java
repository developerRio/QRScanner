package com.originalstocks.qrscanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.originalstocks.qrscanner.Adapters.RecyclerAdapter;
import com.originalstocks.qrscanner.Database.Note;
import com.originalstocks.qrscanner.Database.NoteViewModel;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class SavedQRActivity extends AppCompatActivity implements RecyclerAdapter.OnDeleteClickListener {

    private static final int GET_NOTE_ACTIVITY_REQUEST_CODE = 1;
    private Toolbar mToolbar;
    private RecyclerView recyclerView;
    private ImageView mSwitch;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    // Database
    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_qr);

        mSwitch = findViewById(R.id.switchForActionBar);
        recyclerView = findViewById(R.id.recyclerView_qr);
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);

        final RecyclerAdapter recyclerAdapter = new RecyclerAdapter(this, this);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                recyclerAdapter.setmNotes(notes);
            }
        });
        recyclerView.setHasFixedSize(true);

        // Staggered Layout
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);

        mSwitch.setImageDrawable(getDrawable(R.drawable.list));

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (staggeredGridLayoutManager.getSpanCount() == 2) {
                    mSwitch.setImageDrawable(getDrawable(R.drawable.grid));
                    staggeredGridLayoutManager.setSpanCount(1);
                } else {
                    mSwitch.setImageDrawable(getDrawable(R.drawable.list));
                    staggeredGridLayoutManager.setSpanCount(2);
                }

            }
        });
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }
    @Override
    public void OnDeleteClickListener(Note myNote) {
        noteViewModel.deleteNote(myNote);
    }
}
