# RoomDataBase

# Room is a persistence library, part of the Android Jetpack.
The Room is now considered as a better approach for data storing than SQLiteDatabase.
The Room persistence library provides an abstraction layer over SQLite to allow for more robust database access while harnessing the full power of SQLite.



# Room has 3 main components

Entity: Represents table within the database. We can create a table in room database using @Entity annotation
Dao: Contains all the methods used for accessing data from the database.
Database: Contains the database holder and serves as the main access point for the underlying connection to your app’s persisted, relational data.
