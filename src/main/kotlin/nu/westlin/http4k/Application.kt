package nu.westlin.http4k

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer

// Inspiration: https://kotlinexpertise.com/kotlin-http4k/ and https://www.http4k.org/

data class Car(val regNo: String, val brand: String, val model: String, val year: Int)

class CarRepository(carList: List<Car>) {

    private val cars = carList.toMutableList()

    fun all(): List<Car> {
        return cars
    }

    fun addCar(car: Car) {
        if (cars.any { it == car }) {
            throw CarAlreadyExistException(car)
        }
        cars.add(car)
    }

    fun getByRegNo(regNo: String): Car? {
        return cars.firstOrNull { it.regNo == regNo }
    }
}

class CarAlreadyExistException(car: Car) : RuntimeException("Car $car already exists")

class CarHandlerProvider(private val repository: CarRepository) {

    fun allCarsHandler(): HttpHandler = { Response(OK).with(carListLens of repository.all()) }

    fun putCarHandler(): HttpHandler = securityFilter.then { request: Request ->
        try {
            repository.addCar(carLens.extract(request))
            Response(OK)
        } catch (e: CarAlreadyExistException) {
            Response(BAD_REQUEST).body(e.localizedMessage)
        }
    }

    fun getCarByRegNoHandler(): HttpHandler = { request: Request ->
        repository.getByRegNo(request.path("regNo")!!)?.let {
            Response(OK).with(carLens of it)
        } ?: Response(NOT_FOUND)
    }

    companion object {
        val carLens = Body.auto<Car>().toLens()
        val carListLens = Body.auto<List<Car>>().toLens()
        val regNoLens = Path.string().of("regNo")
    }
}

// TODO petves: Should only exist for tests?
val objectMapper = ObjectMapper().registerKotlinModule()

val pingPongHandler: HttpHandler = { Response(OK).body("Pong!") }

val marcoPoloHandler: HttpHandler = { request: Request ->
    if (request.query("name") == "Marco") {
        Response(OK).body("Polo!")
    } else {
        Response(BAD_REQUEST)
    }
}

val initialCarList = listOf(
    Car("AKU671", "Porsche", "997", 2001),
    Car("KIB946", "Ferrari", "LaFerrari", 2011),
    Car("NMO996", "Volvo", "142", 1972)
)
val carHandlerProvider = CarHandlerProvider(
    // TODO petves: Yes I know it is ugly to that the application contains data by default at starttime
    //  but it is an example application, ok? :)
    CarRepository(initialCarList)
)

private val securityFilter: Filter = ServerFilters.BasicAuth("realm", "admin", "password")

val routing: RoutingHttpHandler = routes(
    "/ping" bind GET to pingPongHandler,
    "/marco" bind GET to marcoPoloHandler,
    "/cars" bind GET to carHandlerProvider.allCarsHandler(),
    "/cars" bind POST to carHandlerProvider.putCarHandler(),
    "/cars/regNo/{regNo}" bind GET to carHandlerProvider.getCarByRegNoHandler()
)

val requestTimeLogger: Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val start = System.currentTimeMillis()
        val response = next(request)
        val execTime = System.currentTimeMillis() - start
        log { "Request ${request.method} to ${request.uri} took $execTime ms" }
        response
    }
}


val server = requestTimeLogger
    .then(routing)
    .asServer(SunHttp(8080))

fun main() {
    log { "Server starting..." }

    log { "Server started on port ${server.start().port()}" }
}

