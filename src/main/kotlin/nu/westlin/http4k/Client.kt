@file:Suppress("unused")

package nu.westlin.http4k

import nu.westlin.http4k.CarHandlerProvider.Companion.carLens
import nu.westlin.http4k.CarHandlerProvider.Companion.carListLens
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters

fun main() {
    val client: HttpHandler = JavaHttpClient()

    //ping(client)
    //listAllCars(client)
    addNewCar(client)
    //getCarByRegNo(client)
}

fun getCarByRegNo(client: HttpHandler) {
    val car = Car("AKU671", "Porsche", "997", 2001)
    val request = Request(GET, "http://localhost:8080/cars/regNo/${car.regNo}")
    val response = client(request)
    log { "response = $response" }
    log { "carListLens.extract(response) = ${carLens.extract(response)}" }
}

fun addNewCar(client: HttpHandler) {
    val car = Car("HOH554", "Mazda", "Miata", 1993)
    val request = Request(POST, "http://localhost:8080/cars").with(carLens of car)
    val response = ClientFilters.BasicAuth("admin", "password").then(client)(request)
    log { "response = $response" }
}

fun listAllCars(client: HttpHandler) {
    val request = Request(GET, "http://localhost:8080/cars")
    val response = client(request)
    log { "response = $response" }
    log { "carListLens.extract(response) = ${carListLens.extract(response)}" }
}

private fun ping(client: HttpHandler) {
    val request = Request(GET, "http://localhost:8080/ping")
    val response = client(request)
    log { "response = ${response.status}" }
    log { "response = $response" }
}
