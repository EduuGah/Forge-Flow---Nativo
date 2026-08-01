package com.forgeflow.core.model

import java.text.Normalizer
import java.util.Locale

fun String.normalizedSearchText(): String = Normalizer.normalize(
    trim().lowercase(Locale.getDefault()),
    Normalizer.Form.NFD,
)
    .replace("\\p{Mn}+".toRegex(), "")
    .replace("\\s+".toRegex(), " ")

fun Exercise.searchTerms(): String {
    val normalizedName = name.normalizedSearchText()
    val aliases = EXERCISE_ALIASES
        .filterKeys(normalizedName::contains)
        .values
        .flatten()
    return (listOf(name) + aliases).joinToString(" ").normalizedSearchText()
}

fun Exercise.matchesSearch(query: String): Boolean {
    val normalizedQuery = query.normalizedSearchText()
    return normalizedQuery.isBlank() || searchTerms().contains(normalizedQuery)
}

private val EXERCISE_ALIASES = mapOf(
    "supino" to listOf("bench press", "press de peito"),
    "supino fechado" to listOf("close grip bench press"),
    "agachamento" to listOf("squat"),
    "agachamento frontal" to listOf("front squat"),
    "agachamento bulgaro" to listOf("bulgarian split squat", "afundo bulgaro"),
    "agachamento goblet" to listOf("goblet squat", "agachamento calice"),
    "agachamento no smith" to listOf("smith squat", "smith machine squat"),
    "hack squat" to listOf("agachamento hack", "hack machine"),
    "levantamento terra" to listOf("deadlift", "terra"),
    "levantamento terra sumo" to listOf("sumo deadlift"),
    "levantamento terra romeno" to listOf("romanian deadlift", "rdl", "terra romeno"),
    "stiff" to listOf("romanian deadlift", "rdl", "terra romeno"),
    "remada" to listOf("row"),
    "remada baixa no cabo" to listOf("seated cable row", "remada sentada"),
    "remada unilateral com halter" to listOf("dumbbell row", "serrote"),
    "remada t" to listOf("t bar row", "cavalinho"),
    "remada alta" to listOf("upright row"),
    "puxada" to listOf("pulldown", "lat pulldown"),
    "pulldown com bracos retos" to listOf("straight arm pulldown", "pullover no cabo"),
    "barra fixa" to listOf("pull up", "chin up"),
    "flexao" to listOf("push up", "apoio"),
    "crossover" to listOf("cable crossover", "crucifixo no cabo"),
    "mergulho" to listOf("dip", "paralelas"),
    "rack pull" to listOf("levantamento terra parcial"),
    "extensao lombar" to listOf("hiperextensao", "banco romano", "back extension"),
    "encolhimento" to listOf("shrug"),
    "rosca" to listOf("curl", "biceps curl"),
    "rosca com halteres" to listOf("rosca direta com halteres", "dumbbell curl"),
    "rosca alternada" to listOf(
        "rosca direta com halter alternado",
        "rosca direta com halteres alternada",
        "alternating dumbbell curl",
    ),
    "rosca scott" to listOf("preacher curl", "banco scott"),
    "rosca martelo" to listOf("hammer curl"),
    "triceps" to listOf("triceps extension"),
    "triceps na polia" to listOf("pushdown", "pulley triceps"),
    "triceps corda" to listOf("rope pushdown"),
    "triceps testa" to listOf("skull crusher", "lying triceps extension"),
    "triceps frances" to listOf("french press", "overhead triceps extension"),
    "coice de triceps" to listOf("triceps kickback", "kickback"),
    "cadeira extensora" to listOf("extensora", "leg extension"),
    "cadeira flexora" to listOf("flexora", "leg curl"),
    "mesa flexora" to listOf("flexora deitada", "lying leg curl"),
    "flexora em pe" to listOf("standing leg curl"),
    "panturrilha" to listOf("calf raise", "gemeos"),
    "elevacao lateral" to listOf("lateral raise"),
    "elevacao frontal" to listOf("front raise"),
    "elevacao pelvica" to listOf("hip thrust"),
    "desenvolvimento" to listOf("shoulder press", "overhead press"),
    "desenvolvimento arnold" to listOf("arnold press"),
    "crucifixo" to listOf("chest fly", "fly"),
    "crucifixo inverso" to listOf("reverse fly", "rear delt fly"),
    "peck deck" to listOf("voador", "crucifixo maquina"),
    "face pull" to listOf("puxada facial"),
    "paralelas" to listOf("dips"),
    "abdominal" to listOf("crunch"),
    "abdominal bicicleta" to listOf("bicycle crunch"),
    "abdominal reverso" to listOf("reverse crunch"),
    "abdominal completo" to listOf("sit up"),
    "prancha" to listOf("plank"),
    "giro russo" to listOf("russian twist"),
    "escalador" to listOf("mountain climber"),
    "elevacao de pernas" to listOf("leg raise"),
    "elevacao de joelhos" to listOf("hanging knee raise"),
    "ab wheel" to listOf("roda abdominal"),
    "dead bug" to listOf("inseto morto"),
    "wood chop" to listOf("lenhador", "cable wood chop"),
    "afundo" to listOf("avanco", "passada", "lunge"),
    "avanco" to listOf("afundo", "passada", "walking lunge"),
    "leg press" to listOf("prensa de pernas", "leg press 45"),
    "good morning" to listOf("bom dia"),
    "ponte de gluteos" to listOf("glute bridge"),
    "pull through" to listOf("cable pull through"),
)
