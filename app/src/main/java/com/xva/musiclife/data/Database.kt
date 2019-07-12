package com.xva.musiclife.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class Database(val context: Context) :
    SQLiteOpenHelper(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION) {


    private val TABLE_SONG = "Song"
    private val COL_SONG_ID = "song_id"
    private val COL_SONG_PATH = "song_path"
    private val COL_SONG_PHOTO = "song_photo"

    private val TABLE_PLAYLIST = "Playlist"
    private val COL_PLAYLIST_ID = "photo_id"
    private val COL_PLAYLIST_NAME = "photo_name"
    private val COL_PLAYLIST_SONG_COUNT = "photo_song_count"

    private val TABLE_FAVOURITE = "Favourite"
    private val COL_FAVOURITE_ID = "favourite_id"

    private val TABLE_BEST_PART = "BestPart"
    private val COL_BEST_PART_ID = "best_part_id"
    private val COL_START_TIME = "start_time"
    private val COL_FINISH_TIME = "finish_time"


    private val TABLE_GESTURE = "Gesture"
    private val COL_GESTURE_ID = "gesture_id"
    private val COL_MAX_VOLUME = "max_volume"

    private val TABLE_SONG_TO_PLAYLIST = "SongToPlaylist"
    private val COL_STP_ID = "stp_id"


    companion object {
        private const val DATABASE_NAME = "PhotoDate"//database adı
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {


        val createDateTable =
            "CREATE TABLE $TABLE_SONG ($COL_SONG_ID INTEGER PRIMARY KEY AUTOINCREMENT , $COL_SONG_PATH TEXT UNIQUE ,$COL_SONG_PHOTO BLOB DEFAULT NULL)  "

        db?.execSQL(createDateTable)

        val createPlaylistTable =
            "CREATE TABLE $TABLE_PLAYLIST ($COL_PLAYLIST_ID INTEGER PRIMARY KEY AUTOINCREMENT , $COL_PLAYLIST_NAME VARCHAR(200) , $COL_PLAYLIST_SONG_COUNT INTEGER DEFAULT 0)"

        db?.execSQL(createPlaylistTable)


        val createFavouriteTable =
            "CREATE TABLE $TABLE_FAVOURITE ($COL_FAVOURITE_ID INTEGER PRIMARY KEY AUTOINCREMENT , $COL_SONG_ID INTEGER UNIQUE , CONSTRAINT FavouriteToSong FOREIGN KEY ($COL_SONG_ID) REFERENCES $TABLE_SONG ($COL_SONG_ID))"

        db?.execSQL(createFavouriteTable)


        val createBestPartTable =
            "CREATE TABLE $TABLE_BEST_PART ($COL_BEST_PART_ID INTEGER PRIMARY KEY AUTOINCREMENT , $COL_SONG_ID INTEGER UNIQUE , $COL_START_TIME FLOAT DEFAULT -1 , $COL_FINISH_TIME FLOAT DEFAULT -1 , CONSTRAINT BPToSong FOREIGN KEY ($COL_SONG_ID) REFERENCES $TABLE_SONG ($COL_SONG_ID))"

        db?.execSQL(createBestPartTable)

        val createGestureTable =
            "CREATE TABLE $TABLE_GESTURE ($COL_GESTURE_ID INTEGER PRIMARY KEY AUTOINCREMENT , $COL_SONG_ID INTEGER , $COL_START_TIME FLOAT  DEFAULT -1 , $COL_FINISH_TIME FLOAT DEFAULT -1 , $COL_MAX_VOLUME INTEGER DEFAULT -1 , CONSTRAINT GestureToSong FOREIGN KEY ($COL_SONG_ID) REFERENCES $TABLE_SONG ($COL_SONG_ID))"

        db?.execSQL(createGestureTable)


        val createSongToPlaylist =
            "CREATE TABLE $TABLE_SONG_TO_PLAYLIST ($COL_STP_ID INTEGER PRIMARY KEY AUTOINCREMENT , $COL_SONG_ID INTEGER , $COL_PLAYLIST_ID INTEGER , CONSTRAINT STPToSong FOREIGN KEY ($COL_SONG_ID) REFERENCES $TABLE_SONG ($COL_SONG_ID) , CONSTRAINT STPToPlaylist FOREIGN KEY ($COL_PLAYLIST_ID) REFERENCES $TABLE_PLAYLIST ($COL_PLAYLIST_ID))"

        db?.execSQL(createSongToPlaylist)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }


    // Şarkı Kayıdı Açılmış mı kontrol et
    internal fun isSongAvailable(path: String): Int {
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_SONG WHERE $COL_SONG_PATH = ?"
        val result = sqliteDB.rawQuery(query, Array<String>(1) { path })

        while (result.moveToNext()) {
            return result.getInt(0)
        }
        sqliteDB.close()
        return -1

    }

    internal fun addSong(path: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_SONG_PATH, path)
        //cv.put(COL_SONG_PHOTO, image)
        db.insert(TABLE_SONG, null, cv)
        db.close()
    }


    internal fun updatePhoto(image: ByteArray, path: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_SONG_PHOTO, image)
        db.update(TABLE_SONG, cv, "$COL_SONG_PATH = ?", Array<String>(1) { path })
        db.close()
/*
        contentValues.put(COL_PHOTO, photo.image)
        contentValues.put(COL_DATE_ID, photo.date_id)
        sqliteDB.insert(TABLE_PHOTO, null, contentValues)
        */
    }


    internal fun getPhoto(path: String): ByteArray? {
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_SONG WHERE $COL_SONG_PATH = ?"
        val result = sqliteDB.rawQuery(query, Array<String>(1) { path })
        while (result.moveToNext()) {
            return result.getBlob(1)
        }

        sqliteDB.close()
        return null
    }

    // Mini Pl , Player Activity ve Song Settings De Kontrol Edilecek
    internal fun isSongAddedToFavourite(id: Int): Int {
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_FAVOURITE WHERE $COL_SONG_ID = ?"
        val result = sqliteDB.rawQuery(query, Array<String>(1) { id.toString() })
        while (result.moveToNext()) {
            return result.getInt(1)
        }
        sqliteDB.close()
        return -1
    }

    internal fun addToFavourite(id: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_SONG_ID, id)
        db.insert(TABLE_FAVOURITE, null, cv)
        db.close()
    }

    internal fun removeFromFavourite(id: Int) {
        val sqliteDB = this.writableDatabase
        val query = "DELETE  FROM $TABLE_FAVOURITE WHERE $COL_SONG_ID = ?"
        val result = sqliteDB.rawQuery(query, Array<String>(1) { id.toString() })
        result.moveToFirst()
    }

    internal fun addPlayList(name: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_PLAYLIST_NAME, name)
        db.insert(TABLE_PLAYLIST, null, cv)
        db.close()
    }

    internal fun updatePlayListSongCount(count: Int, name: String) {

        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_PLAYLIST_SONG_COUNT, count)
        db.update(TABLE_PLAYLIST, cv, "$COL_PLAYLIST_NAME = ?", Array<String>(1) { name })
        db.close()
    }

    internal fun addSongToPlayList(songId: Int, playlistId: Int) {

        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_SONG_ID, songId)
        cv.put(COL_PLAYLIST_ID, playlistId)
        db.insert(TABLE_SONG_TO_PLAYLIST, null, cv)
        db.close()

    }


/*
    // DATES
    internal fun setDate(date: Dat) {
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_DATE, date.date)
        contentValues.put(COL_PHOTO_COUNT, date.photoCount)
        contentValues.put(COL_TAG, date.tag)
        sqliteDB.insert(TABLE_DATE, null, contentValues)
    }

    internal fun getDate(temp: String): Dat? {
        var date = Dat(-1, "", 0, context.resources.getString(R.string.text_tag_it))
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_DATE WHERE $COL_DATE = ?"
        val result = sqliteDB.rawQuery(query, Array<String>(1) { temp })
        if (result.moveToFirst()) {
            date = Date(
                result.getInt(0),
                result.getString(result.getColumnIndex(COL_DATE)),
                result.getInt(2),
                result.getString(result.getColumnIndex(COL_TAG))
            )
        }

        return date
    }

    internal fun getAllDates(): ArrayList<Date> {

        var lastId = getDateMaxId()
        var dates = ArrayList<Date>()
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_DATE WHERE $COL_DATE_ID < ($lastId)"
        val result = sqliteDB.rawQuery(query, null)

        while (result.moveToNext()) {
            var date = Date(
                result.getInt(0),
                result.getString(result.getColumnIndex(COL_DATE)),
                result.getInt(2),
                result.getString(result.getColumnIndex(COL_TAG))
            )
            dates.add(date)
        }


        return dates
    }


    // For Date Table
    internal fun updatePhotoCount(count: Int, id: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_PHOTO_COUNT, count)
        db.update(TABLE_DATE, cv, "$COL_DATE_ID = ?", Array<String>(1) { id.toString() })
    }


    // PHOTOS






    internal fun getPhotoMaxId(): Int {
        val sqliteDB = this.readableDatabase
        val query = "SELECT MAX($COL_PHOTO_ID) FROM $TABLE_PHOTO"
        val result = sqliteDB.rawQuery(query, null)

        while (result.moveToNext()) {
            return result.getInt(0)
        }

        return 0

    }

    internal fun getDateMaxId(): Int {
        val sqliteDB = this.readableDatabase
        val query = "SELECT MAX($COL_DATE_ID) FROM $TABLE_DATE"
        val result = sqliteDB.rawQuery(query, null)

        while (result.moveToNext()) {
            return result.getInt(0)
        }

        return 0
    }

*/
}