package nu.westlin.http4k

import nu.westlin.http4k.CarHandlerProvider.Companion.carLens
import nu.westlin.http4k.CarHandlerProvider.Companion.carListLens
import org.assertj.core.api.Assertions.assertThat
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.UriTemplate
import org.http4k.core.with
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

// TODO petves: Maybe rename this class to CarIntegrationTest?
@Suppress("unused")
internal class ApplicationIntegrationTest {

    private val client: HttpHandler = JavaHttpClient()

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
        val car = initialCarList.first()
        client(
            Request(
                GET,
                UriTemplate.from("$baseUrl/cars/regNo/{regNo}")
            ).with(CarHandlerProvider.regNoLens of car.regNo)
        ).let { response ->
            assertThat(response.status).isEqualTo(OK)
            assertThat(carLens.extract(response)).isEqualTo(car)
        }
    }

    @Test
    fun `get car by registration number that does not exist`() {
        val car = initialCarList.first()
        client(Request(GET, "$baseUrl/cars/${car.regNo}")).let { response ->
            assertThat(response.status).isEqualTo(NOT_FOUND)
            assertThat(response.bodyString()).isEmpty()
        }
    }

    @Test
    fun `get all cars`() {
        // TODO petves: Yes, I know it is ugly to that the application contains data by default at starttime
        //  but it is an example application, ok? :)

        client(Request(GET, "$baseUrl/cars")).let { response ->
            assertThat(response.status).isEqualTo(OK)
            assertThat(carListLens.extract(response)).containsAll(initialCarList)
        }
    }

    @Test
    fun `put a car`() {
        val car = Car("FUK721", "Saab", "99", 1981)
        val response = client(Request(POST, "$baseUrl/cars").with(CarHandlerProvider.carLens of car))

        assertThat(response.status).isEqualTo(OK)
    }

    @Test
    fun `put a car that already exists`() {
        val car = Car("OLA091", "Saab", "99", 1981)

        with(client(Request(POST, "$baseUrl/cars").with(CarHandlerProvider.carLens of car))) {
            assertThat(status).isEqualTo(OK)
        }
        with(client(Request(POST, "$baseUrl/cars").with(CarHandlerProvider.carLens of car))) {
            assertThat(status).isEqualTo(BAD_REQUEST)
        }
    }
}