package com.originalstocks.qrscanner.Database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String mNote;

    @NonNull
    private String imagePath;

    public Note(@NonNull String id, @NonNull String mNote, @NonNull String imagePath) {
        this.id = id;
        this.mNote = mNote;
        this.imagePath = imagePath;
    }

    @NonNull
    public String getImagePath() {
        return imagePath;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getNote() {
        return this.mNote;
    }
}

