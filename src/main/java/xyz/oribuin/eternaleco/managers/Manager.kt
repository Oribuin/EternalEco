package xyz.oribuin.eternaleco.managers

import xyz.oribuin.eternaleco.EternalEco

abstract class Manager(val plugin: EternalEco) {
    abstract fun reload()
    abstract fun disable()
}