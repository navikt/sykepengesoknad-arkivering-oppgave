package no.nav.syfo.config.unleash.strategy

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ByEnvironmentStrategyTest {
    @Test
    fun getName() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.name == "byEnvironment")
    }

    @Test
    fun isEnabledParametersIsNull() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(null)).isFalse()
    }

    @Test
    fun isEnabledParametersIsEmpty() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(HashMap())).isFalse()
    }

    @Test
    fun isEnabledParametersContainingWrongKey() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(
                object : HashMap<String, String>() {
                    init {
                        put("wrongKey", "value")
                    }
                })).isFalse()
    }

    // TODO: Kotlin godtar ikke dette...
//    @Test
//    fun isEnabledParametersContainingNullKey() {
//        val strategy = ByEnvironmentStrategy("local")
//        assertThat(strategy.isEnabled(
//                object : HashMap<String, String>() {
//                    init {
//                        put(null, null)
//                    }
//                })).isFalse()
//    }
//
//    @Test
//    fun isEnabledParametersContainingNullValue() {
//        val strategy = ByEnvironmentStrategy("local")
//        assertThat(strategy.isEnabled(
//                object : HashMap<String, String>() {
//                    init {
//                        put("miljø", null)
//                    }
//                })).isFalse()
//    }

    @Test
    fun isEnabledParametersContainingWrongEnvironment() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(
                object : HashMap<String, String>() {
                    init {
                        put("miljø", "lacol")
                    }
                })).isFalse()
    }

    @Test
    fun isEnabledParametersContainingRightEnvironment() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(
                object : HashMap<String, String>() {
                    init {
                        put("miljø", "local")
                    }
                })).isTrue()
    }

    @Test
    fun isEnabledParametersContainingMultipleEnvironments() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(
                object : HashMap<String, String>() {
                    init {
                        put("miljø", "lacol,local,callo")
                    }
                })).isTrue()
    }

    @Test
    fun isEnabledParametersContainingMultipleEnvironmentsWithSpaces() {
        val strategy = ByEnvironmentStrategy("local")
        assertThat(strategy.isEnabled(
                object : HashMap<String, String>() {
                    init {
                        put("miljø", "lacol ,  , local , callo")
                    }
                })).isTrue()
    }
}