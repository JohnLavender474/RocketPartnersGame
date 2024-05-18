package com.rocketpartners.game.assets

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array

const val TEXTURE_ASSET_PREFIX = "sprites/sprite_sheets/"

enum class TextureAsset(src: String) : IAsset {
    ;

    companion object {
        fun valuesAsIAssetArray(): Array<IAsset> {
            val assets = Array<IAsset>()
            entries.forEach { assets.add(it) }
            return assets
        }
    }

    override val source = TEXTURE_ASSET_PREFIX + src
    override val assClass = TextureAtlas::class.java
}
