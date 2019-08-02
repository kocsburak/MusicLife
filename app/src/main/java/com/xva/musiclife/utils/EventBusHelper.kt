package com.xva.musiclife.utils

import com.xva.musiclife.models.Song
import com.xva.musiclife.services.PlayerServicesBinder

class EventBusHelper {

    internal class playingSong(var song: Song)
    internal class isShowMiniPl(var status: String)
    internal class songInformations(var song: Song)
    internal class songsQueue(var queue: ArrayList<Song>)
    internal class songForQueue(var song: Song)
    internal class playList(var playList: ArrayList<Song>,var playListSongIndex:Int)
    internal class removePlayList(var status: Boolean)
    internal class favouriteStatus(var status: Boolean)
    internal class serviceBinder(var serviceBinder:PlayerServicesBinder?)
}