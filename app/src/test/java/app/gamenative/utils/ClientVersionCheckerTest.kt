package app.gamenative.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClientVersionCheckerTest {

    private val buildInfo = """
        Branch!STRING:0|Active!DEC:1|Version!STRING:0|Product!STRING:0
        us|0|1.60.1.70009|wow_classic_beta
        us|1|1.60.1.70450|wowf
    """.trimIndent()

    @Test
    fun parseBuildInfo_usesWowfRow() {
        assertEquals(70450, ClientVersionChecker.parseBuildInfo(buildInfo))
    }

    @Test
    fun parseBuildInfo_withoutWowfRow_returnsNull() {
        val betaOnly = buildInfo.replace("|wowf", "|other_product")
        assertNull(ClientVersionChecker.parseBuildInfo(betaOnly))
    }

    @Test
    fun parseBuildNumber_takesLastNumericSegment() {
        assertEquals(70450, ClientVersionChecker.parseBuildNumber("1.60.1.70450"))
        assertNull(ClientVersionChecker.parseBuildNumber("1.60.1.beta"))
    }

    @Test
    fun parseRequirement_readsOptionalBounds() {
        assertEquals(
            ServerBuildRequirement(70400, 70500),
            ClientVersionChecker.parseRequirement(
                "portal=auth.example.com\n${ClientVersionChecker.PROP_MIN_BUILD}=70400\n" +
                    "${ClientVersionChecker.PROP_MAX_BUILD}=70500\n",
            ),
        )
        assertEquals(
            ServerBuildRequirement(null, null),
            ClientVersionChecker.parseRequirement("${ClientVersionChecker.PROP_MIN_BUILD}=nope"),
        )
    }

    @Test
    fun parseRequirement_ignoresInvertedRange() {
        assertEquals(
            ServerBuildRequirement(),
            ClientVersionChecker.parseRequirement(
                "${ClientVersionChecker.PROP_MIN_BUILD}=70500\n${ClientVersionChecker.PROP_MAX_BUILD}=70400",
            ),
        )
    }

    @Test
    fun validateAgainstServer_handlesBoundaries() {
        val exact = ServerBuildRequirement(70450, 70450)
        assertEquals(
            ClientCompatibility.TooOld(70449, exact),
            ClientVersionChecker.validateAgainstServer(70449, exact),
        )
        assertEquals(
            ClientCompatibility.Compatible(70450, exact),
            ClientVersionChecker.validateAgainstServer(70450, exact),
        )
        assertEquals(
            ClientCompatibility.TooNew(70451, exact),
            ClientVersionChecker.validateAgainstServer(70451, exact),
        )
        assertEquals(
            ClientCompatibility.TooNew(null, exact),
            ClientVersionChecker.validateAgainstServer(null, exact),
        )
    }
}
