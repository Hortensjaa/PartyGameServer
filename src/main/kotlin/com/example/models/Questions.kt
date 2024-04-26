package com.example.models

object Questions {
    val questions = listOf(
        // who...
        Question(Headers.WHO, "lies the most?"),
        Question(Headers.WHO, "will be the best parent?"),
        Question(Headers.WHO, "is the most frequently late?"),
        Question(Headers.WHO, "is the most creative?"),

        // who said that?
        Question(Headers.SAY, "\"I've got new idea for tax fiddle\""),
        Question(Headers.SAY, "\"I met him/her yesterday, but I know it's love of my life\""),
        Question(Headers.SAY, "\"I'm bored of this game\""),
        Question(Headers.SAY, "\"I would do it better\""),

        // who is more likely to do that?
        Question(Headers.DO, "Get back together with ex"),
        Question(Headers.DO, "Gossip about someone and not notice that they are standing behind you."),
        Question(Headers.DO, "Die in a very dumb way"),
        Question(Headers.DO, "Go to jail"),

    )
}