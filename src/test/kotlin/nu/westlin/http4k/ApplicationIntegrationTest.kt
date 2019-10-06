package nu.westlin.http4k

import nu.westlin.http4k.CarHandlerProvider.Companion.carLens
import nu.westlin.http4k.CarHandlerProvider.Companion.carListLens
import nu.westlin.http4k.CarHandlerProvider.Companion.regNoLens
import org.assertj.core.api.Assertions.assertThat
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Method.*
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ClientFilters
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

// TODO: Maybe rename this class to CarIntegrationTest?
@Suppress("unused")
internal class ApplicationIntegrationTest {

    private val client: HttpHandler = JavaHttpClient()

    private val securityFilter: Filter = ClientFilters.BasicAuth("admin", "password")


    private val baseUrl = "http://localhost:8080"

    @BeforeAll
    private fun startup() {
        server.start()
    }

    @AfterAll
    private fun shutdown() {
        server.stop()
    }

    @Test
    fun `get car by registration number`() {
        val car = postCar(Car.random())
        client(
            Request(
                GET,
                UriTemplate.from("$baseUrl/cars/{regNo}")
            ).with(regNoLens of car.regNo)
        ).let { response ->
            assertThat(response.status).isEqualTo(OK)
            assertThat(carLens.extract(response)).isEqualTo(car)
        }
    }

    @Test
    fun `get car by registration number that does not exist`() {
        client(Request(GET, "$baseUrl/cars/doesNotExist")).let { response ->
            assertThat(response.status).isEqualTo(NOT_FOUND)
            assertThat(response.bodyString()).isEmpty()
        }
    }

    @Test
    fun `get all cars`() {
        val cars = generateSequence { postCar(Car.random()) }.take(3).toList()

        client(Request(GET, "$baseUrl/cars")).let { response ->
            assertThat(response.status).isEqualTo(OK)
            assertThat(carListLens.extract(response)).containsAll(cars)
        }
    }

    private fun postCar(car: Car): Car {
        val response = securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))

        assertThat(response.status).isEqualTo(CREATED)

        return car
    }

    @Test
    fun `post a car`() {
        val car = Car.random()
        val response = securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))

        assertThat(response.status).isEqualTo(CREATED)
    }

    @Test
    fun `post a car that already exist`() {
        val car = Car.random()

        with(securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))) {
            assertThat(status).isEqualTo(CREATED)
        }
        with(securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))) {
            assertThat(status).isEqualTo(CONFLICT)
        }
    }

    @Test
    fun `put a car that already exist`() {
        val originalCar = initialCarList.first()
        val updatedCar = originalCar.copy(model = "Cayenne", year = originalCar.year + 1)

        with(
            securityFilter.then(client)(
                Request(
                    PUT,
                    UriTemplate.from("$baseUrl/cars/{regNo}")
                )
                    .with(regNoLens of updatedCar.regNo)
                    .with(carLens of updatedCar)
            )
        ) {
            assertThat(status).isEqualTo(OK)
        }
    }

    @Test
    fun `put a car that does not exist`() {
        val car = Car.random()

        with(
            securityFilter.then(client)(
                Request(
                    PUT,
                    UriTemplate.from("$baseUrl/cars/{regNo}")
                )
                    .with(regNoLens of car.regNo)
                    .with(carLens of car)
            )
        ) {
            assertThat(status).isEqualTo(NOT_FOUND)
        }
    }
}