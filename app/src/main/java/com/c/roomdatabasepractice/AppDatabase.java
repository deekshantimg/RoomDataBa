package com.c.roomdatabasepractice;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class},version = 1)
public  abstract  class AppDatabase extends RoomDatabase {
    public abstract DataDao dataDao();
}
