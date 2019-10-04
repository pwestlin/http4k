package nu.westlin.http4k

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CarHandlerProviderTest {
    private val repository = mockk<CarRepository>()
    private val provider = CarHandlerProvider(repository)

    private val cars = listOf(
        Car("Porsche", "997", 2001),
        Car("Ferrari", "LaFerrari", 2011),
        Car("Volvo", "142", 1972)
    )

    @BeforeEach
    private fun init() {
        every { repository.all() }.returns(cars)
    }

    @Test
    fun `all cars`() {
        val handler = provider.allCarsHandler()
        val response = handler(Request(Method.GET, "/cars"))

        // TODO petves: Extension function or similar on assertThat that can do like
        // assertThat(response).hasStatus(Status.OK)
        assertThat(response.status).isEqualTo(Status.OK)
        assertThat(provider.carListLens.extract(response)).containsExactlyInAnyOrderElementsOf(cars)
    }

    @Test
    fun `put a car`() {
        val car = Car("Saab", "99", 1981)
        every { repository.addCar(car) }.returns(Unit)

        val handler = provider.putCarHandler()
        val response = handler(Request(Method.POST, "/cars").with(provider.carLens of car))

        assertThat(response.status).isEqualTo(Status.OK)
        assertThat(response.body.toString()).isEmpty()

        verify { repository.addCar(car) }
    }

    @Test
    fun `put a car that already exist`() {
        val car = Car("Saab", "99", 1981)
        every { repository.addCar(car) }.throws(CarAlreadyExistException(car))

        val handler = provider.putCarHandler()
        val response = handler(Request(Method.POST, "/cars").with(provider.carLens of car))

        assertThat(response.status).isEqualTo(Status.BAD_REQUEST)
        assertThat(response.body.toString()).isEqualTo("Car $car already exists")

        verify { repository.addCar(car) }
    }
}