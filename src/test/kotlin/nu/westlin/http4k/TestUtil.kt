package nu.westlin.http4k

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.Year
import java.util.*
import kotlin.streams.asSequence

// TODO petves: Should only exist for tests?
val objectMapper = ObjectMapper().registerKotlinModule()

fun Car.Companion.random(): Car {
    val brandModelMap: Map<String, List<String>> = mapOf(
        "Porsche" to listOf("Cayenne", "911", "997", "917"),
        "Ferrari" to listOf("458", "LaFerrari", "Testarossa", "F40", "F50"),
        "Volvo" to listOf("140", "240", "740", "940", "Amazon", "PV"),
        "Audi" to listOf("A4", "A6", "A8", "R8", "RS4", "100")
    )

    fun randomRegNo(): String {
        val sourceString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return Random().ints(6, 0, sourceString.length)
            .asSequence()
            .map(sourceString::get)
            .joinToString("")
    }

    fun randomBrandWithModel(): Pair<String, String> {
        val brand =  brandModelMap.keys.random()
        val model = (brandModelMap[brand] ?: error("This should NOT happen :)")).random()

        return brand to model
    }

    val (brand, model) = randomBrandWithModel()
    return Car(
        regNo = randomRegNo(),
        brand = brand,
        model = model,
        year = kotlin.random.Random.nextInt(1970, Year.now().value + 1)
    )
}