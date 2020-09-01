package ch15

fun main() {

    Game.play()

    performCombat()
    performCombat("Ulrich")
    performCombat("Hildr", true)
}



object Game {
    private val player = Player("Madrigal")
    private var currentRoom: Room = TownSquare()

    init {
        println("Welcome, adventurer")
        player.castFireball()
    }

    fun play(){
        while(true){
            println(currentRoom.description())
            println(currentRoom.load())
            printPlayerStatus(player)
            print(">Enter your command: ")
            println("Last command: ${readLine()}")
        }
    }

    private fun printPlayerStatus(player: Player){
        println("(Aura: ${player.auraColor()}) " +
                "(Blessed: ${if (player.isBlessed) "YES" else "NO"})")
        println("${player.name} ${player.formatHealthStatus()}")
    }
}


fun performCombat() {
    println("적군이 없다!")
}
fun performCombat(enemyName: String) {
    println("$enemyName 과 전투를 시작함.")
}
fun performCombat(enemyName: String, isBlessed: Boolean) {
    if (isBlessed) {
        println("$enemyName 과 전투를 시작함. 축복을 받음!")
    } else {
        println("$enemyName 과 전투를 시작함.")
    }
}