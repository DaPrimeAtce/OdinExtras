package com.daprimeatce.odinextras.utils

// Started to see duplicate regex usage between files, so regex goes here now or something

// Instanced content regex
//val stormStartRegex = Regex("\\[BOSS] Storm: Pathetic Maxor, just like expected\\.")
val stormEnrageRegex = Regex("^⚠ Storm is enraged! ⚠$")
val stormMoveRegex = Regex("^\\[BOSS] Storm: (ENERGY HEED MY CALL|THUNDER LET ME BE YOUR CATALYST)!$")
val witherKingStartRegex = Regex("^\\[BOSS] Wither King: You\\.\\.\\. again\\?$")
val professorRegex = Regex("^\\[BOSS] The Professor: Oh\\? You found my Guardians' one weakness\\?$")
val startOfDungeonRegex = Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.|\\[NPC] Mort: Right-click the Orb for spells, and Left-click \\(or Drop\\) to use your Ultimate!")
val endOfDungeonRegex = Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?$")
val startOfKuudraRegex = Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$")
val endOfKuudraRegex = Regex("^\\[NPC] Elle: Good job everyone. A hard fought battle come to an end. Let's get out of here before we run into any more trouble!$")

// Slayer regex
val nameRegex = Regex("^Spawned by: (.*)$")
val completeRegex = Regex("^(\\s*)SLAYER QUEST COMPLETE!$")
val failRegex = Regex("^(\\s*)SLAYER QUEST FAILED!$")
val cancelRegex = Regex("^Your Slayer Quest has been cancelled!$")

// Misc regex
val messageRegex = Regex("^(?:Party > (\\[[^]]*?])? ?(\\w{1,16})(?: [ቾ⚒])?: ?(.+)$|Guild > (\\[[^]]*?])? ?(\\w{1,16})(?: \\[([^]]*?)])?: ?(.+)$|(From|To) (\\[[^]]*?])? ?(\\w{1,16}): ?(.+)$)")
val serverRegex = Regex("^Sending to server (.*)...$")
