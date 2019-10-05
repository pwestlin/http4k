package nu.westlin.http4k

import nu.westlin.http4k.CarHandlerProvider.Companion.carLens
import nu.westlin.http4k.CarHandlerProvider.Companion.carListLens
import org.assertj.core.api.Assertions.assertThat
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ClientFilters
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

// TODO petves: Maybe rename this class to CarIntegrationTest?
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
        client(Request(GET, "$baseUrl/cars/regNo/doesNotExist")).let { response ->
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
    fun `post a car`() {
        val car = Car("FUK721", "Saab", "99", 1981)
        val response = securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))

        assertThat(response.status).isEqualTo(CREATED)
    }

    @Test
    fun `post a car that already exists`() {
        val car = Car("OLA091", "Saab", "99", 1981)

        with(securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))) {
            assertThat(status).isEqualTo(CREATED)
        }
        with(securityFilter.then(client)(Request(POST, "$baseUrl/cars").with(carLens of car))) {
            assertThat(status).isEqualTo(BAD_REQUEST)
        }
    }
}