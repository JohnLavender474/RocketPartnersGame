package com.rocketpartners.game.assets

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.utils.Array

const val MUSIC_ASSET_PREFIX = "music/"

enum class MusicAsset(src: String) : IAsset {
    ;

    companion object {
        fun valuesAsIAssetArray(): Array<IAsset> {
            val assets = Array<IAsset>()
            entries.forEach { assets.add(it) }
            return assets
        }
    }

    override val source = MUSIC_ASSET_PREFIX + src
    override val assClass = Music::class.java
}
