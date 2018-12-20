package com.originalstocks.qrscanner.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Note.class, version = 1)
public abstract class NoteRoomDataBase extends RoomDatabase {
    private static volatile NoteRoomDataBase noteRoomInstance;

    static NoteRoomDataBase getDatabase(final Context context) {
        if (noteRoomInstance == null) {
            synchronized (NoteRoomDataBase.class) {
                if (noteRoomInstance == null) {
                    noteRoomInstance = Room.databaseBuilder(context.getApplicationContext(), NoteRoomDataBase.class, "note_database")
                            .build();
                }
            }
        }
        return noteRoomInstance;
    }
    public abstract NoteDao noteDao();
}
