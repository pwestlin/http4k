package nu.westlin.http4k

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RoutesTest {

    private val pingPongHandler: HttpHandler = { Response(OK) }

    private val marcoPoloHandler: HttpHandler = { Response(OK) }
    private val carHandlerProvider = mockk<CarHandlerProvider>()
    private val internalServerErrorHandler: HttpHandler = { Response(INTERNAL_SERVER_ERROR) }

    private lateinit var routes: RoutingHttpHandler

    @BeforeEach
    private fun setupMocks() {
        every { carHandlerProvider.allCarsHandler() } returns { Response(OK) }
        every { carHandlerProvider.postCarHandler() } returns { Response(OK) }
        every { carHandlerProvider.getCarByRegNoHandler() } returns { Response(OK) }
        every { carHandlerProvider.putCarHandler() } returns { Response(OK) }
        routes = Routes(pingPongHandler, marcoPoloHandler, carHandlerProvider, internalServerErrorHandler).routes
    }

    @Test
    fun `ping pong`() {
        assertThat(routes(Request(GET, "/ping")).status).isEqualTo(OK)
    }

    @Test
    fun `pinga ponga`() {
        assertThat(routes(Request(GET, "/pinga")).status).isEqualTo(NOT_FOUND)
    }

    @Test
    fun `marco polo`() {
        assertThat(routes(Request(GET, "/marco")).status).isEqualTo(OK)
    }

    @Test
    fun `all cars`() {
        assertThat(routes(Request(GET, "/cars")).status).isEqualTo(OK)
    }

    @Test
    fun `error should return 500`() {
        assertThat(routes(Request(GET, "/error")).status).isEqualTo(INTERNAL_SERVER_ERROR)
    }
}
