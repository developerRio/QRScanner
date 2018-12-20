package com.originalstocks.qrscanner.Database;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class NoteViewModel extends AndroidViewModel {

    private NoteDao noteDao;
    private NoteRoomDataBase noteDataBase;
    private LiveData<List<Note>> mAllNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);

        noteDataBase = NoteRoomDataBase.getDatabase(application);
        noteDao = noteDataBase.noteDao();
        mAllNotes = noteDao.getAllNotes();
    }

    public void insert(Note note) {
        new InsertAsyncTask(noteDao).execute(note);
    }

    public LiveData<List<Note>> getAllNotes() {
        return mAllNotes;
    }

    public LiveData<Note> getImagePath(String noteIds){
        return noteDao.getImagePath(noteIds);
    }

    public void updateNote(Note note) {
        new UpdateAsyncTask(noteDao).execute(note);
    }

    public void deleteNote(Note note) {
        new DeleteAsyncTask(noteDao).execute(note);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    private class InsertAsyncTask extends AsyncTask<Note, Void, Void> {

        NoteDao noteDao;

        public InsertAsyncTask(NoteDao mNoteDao) {
            this.noteDao = mNoteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {

            noteDao.insertNote(notes[0]);

            return null;
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Note, Void, Void> {
        NoteDao noteDao;

        public UpdateAsyncTask(NoteDao mNoteDao) {
            this.noteDao = mNoteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {

            noteDao.update(notes[0]);

            return null;
        }
    }

    private class DeleteAsyncTask extends AsyncTask<Note, Void, Void> {

        NoteDao mNoteDao;

        public DeleteAsyncTask(NoteDao noteDao) {
            this.mNoteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            mNoteDao.delete(notes[0]);
            return null;
        }
    }
}
