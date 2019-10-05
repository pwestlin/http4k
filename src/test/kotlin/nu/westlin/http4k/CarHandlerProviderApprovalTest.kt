package nu.westlin.http4k

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.lens.Header
import org.http4k.testing.Approver
import org.http4k.testing.JsonApprovalTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * https://www.http4k.org/guide/modules/approvaltests/
 */
@ExtendWith(JsonApprovalTest::class)
internal class CarHandlerProviderApprovalTest {

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
    fun `all cars`(approver: Approver) {
        val handler = provider.allCarsHandler()

        val response = handler(
            Request(
                GET,
                "/cars"
            )
        )
        assertThat(response.status).isEqualTo(Status.OK)
        approver.assertApproved(
            response
                .with(Header.CONTENT_TYPE of ContentType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(initialCarList))
        )
    }

    @Test
    fun `get a car by regNo`(approver: Approver) {
        val car = Car("LEN779", "Saab", "99", 1981)
        every { repository.getByRegNo(car.regNo) }.returns(car)

        val handler = provider.getCarByRegNoHandler()
        val response = handler(
            Request(
                GET,
                UriTemplate.from("cars/regNo/{regNo}")
            ).with(CarHandlerProvider.regNoLens of car.regNo)
        )
        assertThat(response.status).isEqualTo(Status.OK)
        approver.assertApproved(
            response
                .with(Header.CONTENT_TYPE of ContentType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(car))
        )
    }
}