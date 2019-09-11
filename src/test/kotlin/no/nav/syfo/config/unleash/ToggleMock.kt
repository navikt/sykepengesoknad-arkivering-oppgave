package no.nav.syfo.config.unleash

import no.nav.syfo.config.unleash.strategy.ByEnvironmentStrategy
import org.springframework.stereotype.Service
import java.util.stream.Stream.of

@Service
class ToggleMock(private val toggleMockConfig: ToggleMockConfig,
                 private val byEnvironmentStrategy: ByEnvironmentStrategy) : Toggle {

    override fun isEnabled(toggle: FeatureToggle): Boolean {
        val config = toggleMockConfig.getConfig()[toggle]

        val check = of<Boolean>(
                checkEnvironment(config!!)
                //Add more checks here!! N/A if null is returned!
        )
                .filter { it != null }
                .max { obj, b -> obj.compareTo(b) }
                .orElse(true)


        return checkEnabled(config) && check!!
    }

    private fun checkEnabled(config: ToggleMockConfig.Config): Boolean {
        return config.isEnabled
    }

    private fun checkEnvironment(config: ToggleMockConfig.Config): Boolean? {
        return config.byEnvironment?.let { env ->
            byEnvironmentStrategy.isEnabled( mapOf( Pair(
                    "miljø",
                    env.joinToString(","))))
        }
    }
}
