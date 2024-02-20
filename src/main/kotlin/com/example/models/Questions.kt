package com.example.models

object Questions {
    val questions = listOf(
        // kto...
        Question(Headers.WHO, "najwięcej kłamie?"),
        Question(Headers.WHO, "będzie najlepszym rodzicem?"),
        Question(Headers.WHO, "pierwszy zostanie rodzicem?"),
        Question(Headers.WHO, "jest najbardziej kreatywny?"),

        // kto by to powiedział?
        Question(Headers.SAY, "\"Ale mam pomysł na przekręt finansowy\""),
        Question(Headers.SAY, "\"Poznał*m ją/jego wczoraj, ale już wiem, że to miłość\""),
        Question(Headers.SAY, "\"Nudzi mi się ta gra\""),
        Question(Headers.SAY, "\"Ja bym to zrobił* lepiej\""),

        // kto byłby w stanie to zrobić?
        Question(Headers.DO, "Wrócić do swojej/swojego ex"),
        Question(Headers.DO, "Nie zauważyć, że głośno obgaduje osobę, która stoi za nim"),
        Question(Headers.DO, "Zginąć w bardzo głupi sposób"),
        Question(Headers.DO, "Trafić do więzienia"),

    )
}