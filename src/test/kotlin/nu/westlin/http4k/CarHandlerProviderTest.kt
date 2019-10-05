package nu.westlin.http4k

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nu.westlin.http4k.CarHandlerProvider.Companion.carLens
import nu.westlin.http4k.CarHandlerProvider.Companion.carListLens
import nu.westlin.http4k.CarHandlerProvider.Companion.regNoLens
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.filter.ClientFilters
import org.http4k.filter.ServerFilters
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CarHandlerProviderTest {
    private val repository = mockk<CarRepository>()
    private val provider = CarHandlerProvider(repository)

    private val cars = listOf(
        Car("AKU671", "Porsche", "997", 2001),
        Car("KIB946", "Ferrari", "LaFerrari", 2011),
        Car("NMO996", "Volvo", "142", 1972)
    )

    @BeforeEach
    private fun init() {
        every { repository.all() }.returns(cars)
    }

    @Test
    fun `all cars`() {
        val handler = provider.allCarsHandler()
        val response = handler(Request(GET, "/cars"))

        assertThat(response.status).isEqualTo(OK)
        assertThat(carListLens.extract(response)).containsExactlyInAnyOrderElementsOf(cars)
    }

    @Test
    fun `put a car`() {
        val car = Car("LEN779", "Saab", "99", 1981)
        every { repository.addCar(car) }.returns(Unit)

        val handler = provider.putCarHandler()
        val response = ClientFilters.BasicAuth("admin", "password").then(handler)(Request(POST, "/cars").with(carLens of car))

        assertThat(response.status).isEqualTo(OK)
        assertThat(response.bodyString()).isEmpty()

        verify { repository.addCar(car) }
    }

    @Test
    fun `put a car that already exist`() {
        val car = Car("NAO124", "Saab", "99", 1981)
        every { repository.addCar(car) }.throws(CarAlreadyExistException(car))

        val handler = provider.putCarHandler()
        val response = ClientFilters.BasicAuth("admin", "password").then(handler)(Request(POST, "/cars").with(carLens of car))

        assertThat(response.status).isEqualTo(BAD_REQUEST)
        assertThat(response.bodyString()).isEqualTo("Car $car already exists")

        verify { repository.addCar(car) }
    }

    @Test
    fun `get car by regNo`() {
        val car = cars.last()
        every { repository.getByRegNo(car.regNo) }.returns(car)

        val response = provider.getCarByRegNoHandler()(
            Request(
                GET,
                UriTemplate.from("cars/regNo/{regNo}")
            ).with(regNoLens of car.regNo)
        )
        assertThat(response.status).isEqualTo(OK)
        assertThat(carLens.extract(response)).isEqualTo(car)
    }

    @Test
    fun `get car by regNo that does not exist`() {
        val regNo = "does not exist"
        every { repository.getByRegNo(regNo) }.returns(null)

        val response = provider.getCarByRegNoHandler()(
            Request(
                GET,
                UriTemplate.from("cars/regNo/{regNo}")
            ).with(regNoLens of regNo)
        )
        assertThat(response.status).isEqualTo(NOT_FOUND)
        assertThat(response.bodyString()).isEmpty()
    }

    @Test
    fun fails_to_authenticate() {
        val handler = ServerFilters.BasicAuth("my realm", "user", "password").then { Response(OK) }
        val response = handler(Request(GET, "/"))
        assertThat(response.status).isEqualTo(UNAUTHORIZED)
        assertThat(response.header("WWW-Authenticate")).isEqualTo("Basic Realm=\"my realm\"")
    }

    @Test
    fun authenticate_using_client_extension() {
        val handler = ServerFilters.BasicAuth("my realm", "user", "password").then { Response(OK) }
        val response = ClientFilters.BasicAuth("user", "password").then(handler)(Request(GET, "/"))
        assertThat(response.status).isEqualTo(OK)
    }
}