package com.rocketpartners.game.assets

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Array

const val SOUND_ASSET_PREFIX = "sounds/"

enum class SoundAsset(src: String, val seconds: Int) : IAsset {
    ;

    companion object {
        fun valuesAsIAssetArray(): Array<IAsset> {
            val assets = Array<IAsset>()
            entries.forEach { assets.add(it) }
            return assets
        }
    }

    override val source = SOUND_ASSET_PREFIX + src
    override val assClass = Sound::class.java
}
