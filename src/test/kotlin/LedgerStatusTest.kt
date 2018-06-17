import org.junit.jupiter.api.Test

import iroha.getting.started.IrohaClient
import org.junit.jupiter.api.Assertions.assertEquals

class LedgerStatusTest {


    @Test
    fun ledgerTest() {
        try {
            System.loadLibrary("irohajava")
        } catch (e: UnsatisfiedLinkError) {
            System.err.println("Native code library failed to load. \n$e")
            System.exit(1)
        }

        val client = IrohaClient()
        client.run()

        assertEquals(listOf("admin", "user"), client.getRoles())
        assertEquals(13.toLong(), client.getBalance("admin@main","alice@main", "sora#main"))
        assertEquals(37.toLong(), client.getBalance("admin@main", "bob@main", "sora#main"))
        assertEquals(50.toLong(), client.getBalance("admin@main","admin@main", "sora#main"))

    }

}
