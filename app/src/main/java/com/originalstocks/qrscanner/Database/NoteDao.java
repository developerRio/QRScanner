package com.originalstocks.qrscanner.Database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NoteDao {

    @Insert
    void insertNote(Note note);

    @Query("SELECT * FROM notes")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM notes WHERE id=:noteId")
    LiveData<Note> getNote(String noteId);

    @Query("SELECT * FROM notes WHERE id=:noteIds")
    LiveData<Note> getImagePath(String noteIds);

    @Update
    void update(Note note);

    @Delete
    int delete(Note note);

}
