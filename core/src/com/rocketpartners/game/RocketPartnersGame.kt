package com.rocketpartners.game

import com.engine.Game2D
import com.engine.common.extensions.objectSetOf
import com.engine.events.Event
import com.engine.events.IEventListener
import com.rocketpartners.game.events.EventType

class RocketPartnersGame : Game2D(), IEventListener {

    override val eventKeyMask = objectSetOf<Any>(
        EventType.TURN_CONTROLLER_ON,
        EventType.TURN_CONTROLLER_OFF
    )

    override fun create() {

    }

    override fun onEvent(event: Event) {

    }

    override fun render() {

    }

    override fun dispose() {

    }
}
