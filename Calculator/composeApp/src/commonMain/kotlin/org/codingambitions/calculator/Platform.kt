package org.codingambitions.calculator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform