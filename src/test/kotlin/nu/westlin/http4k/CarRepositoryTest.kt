package nu.westlin.http4k

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CarRepositoryTest {

    private val cars = listOf(
        Car("AKU671", "Porsche", "997", 2001),
        Car("KIB946", "Ferrari", "LaFerrari", 2011),
        Car("NMO996", "Volvo", "142", 1972)
    )

    private lateinit var repository: CarRepository

    @BeforeEach
    private fun init() {
        repository = CarRepository().also { repo -> cars.forEach { repo.addCar(it) } }
    }

    @Test
    fun `all cars`() {
        assertThat(repository.all()).containsExactlyInAnyOrderElementsOf(cars)
    }

    @Test
    fun `add a car`() {
        val car = Car.random()
        repository.addCar(car)

        assertThat(repository.all()).contains(car)
    }

    @Test
    fun `add a car that already exists should throw CarAlreadyExistException`() {
        val car = Car.random()
        repository.addCar(car)
        assertThatThrownBy { repository.addCar(car) }
            .isInstanceOf(CarAlreadyExistException::class.java)
            .hasMessage("Car $car already exists")
    }

    @Test
    fun `get by regNo`() {
        val car = cars.last()
        assertThat(repository.getByRegNo(car.regNo)).isEqualTo(car)
    }

    @Test
    fun `get by regNo that does not exist`() {
        assertThat(repository.getByRegNo("does not exist")).isNull()
    }

    @Test
    fun `update car that exist`() {
        val originalCar = Car.random()
        repository.addCar(originalCar)

        val updatedCar = originalCar.copy(brand = "Audi", model = "A6", year = originalCar.year - 2)
        repository.updateCar(updatedCar)

        assertThat(repository.getByRegNo(updatedCar.regNo)).isEqualTo(updatedCar)
    }

    @Test
    fun `update car that does not exist`() {
        val car = Car.random()

        assertThatThrownBy { repository.updateCar(car) }
            .isInstanceOf(CarDoesNotExistException::class.java)
            .hasMessage("Car $car does not exist")
    }
}