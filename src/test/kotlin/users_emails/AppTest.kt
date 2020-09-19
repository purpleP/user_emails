/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package users_emails

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlin.random.Random as rnd
import kotlin.test.assertEquals


class EmailTest : StringSpec({
    val minGroups = 0
    val maxGroups = 10
    "Finds users connected by having common emails" {
        checkAll(Arb.int(minGroups..maxGroups)) { numGroups ->
            val groupsOfNamesWithEmails = namesWithEmailsToBeConnected(numGroups, maxGroups)
            val emailsForName = groupsOfNamesWithEmails
                .map { (names, emails) ->
                    connectUserNamesByCommonEmails(names.toSet(), emails.toSet())
                }
                .fold(mapOf<String, Set<String>>()) { acc, y -> acc + y }
            val grouped = groupUsers(emailsForName)
            groupsOfNamesWithEmails.forAll { (names, emails) ->
                val chosenName = names.firstOrNull { it in grouped }
                chosenName.shouldNotBeNull()
                should {
                    grouped[chosenName].shouldContainExactlyInAnyOrder(emails)
                }
            }
        }
    }
    "Should group users with common emails" {
        val input = mapOf(
            "user1" to setOf("a@b.com", "c@b.com"),
            "user2" to setOf("a@b.com", "d@b.com"),
            "user3" to setOf("e@b.com", "f@b.com"),
        )
        val correct = mapOf(
            "user1" to setOf("a@b.com", "c@b.com", "d@b.com"),
            "user3" to setOf("e@b.com", "f@b.com"),
        )
        val result = groupUsers(input)
        assertEquals(correct, result)
    }
    "Should not group users with no common emails" {
        val input = mapOf(
            "user1" to setOf("a@b.com", "c@b.com"),
            "user2" to setOf("d@b.com"),
            "user3" to setOf("e@b.com", "f@b.com"),
        )
        val correct = mapOf(
            "user1" to setOf("a@b.com", "c@b.com"),
            "user2" to setOf("d@b.com"),
            "user3" to setOf("e@b.com", "f@b.com"),
        )
        val result = groupUsers(input)
        assertEquals(correct, result)
    }
})

private fun namesWithEmailsToBeConnected(
    numGroups: Int,
    maxGroups: Int
): List<Pair<List<String>, List<String>>> {
    val groups = (0 until numGroups)
    val groupedNames = groups
        .map { genUniqueNames(it, rnd.nextInt(1, maxGroups)) }
    val groupedEmails = groups.map { g ->
        genUniqueEmails(g, rnd.nextInt(1, 10))
    }
    return groupedNames.zip(groupedEmails)
}

fun genUniqueNames(group: Int, num: Int) = (0 until num).map { "${group}_$it" }

fun genUniqueEmails(group: Int, num: Int) = (0 until num).map { "$it@$group.com" }

fun <T> MutableCollection<T>.popRandom(): T = random().also { remove(it) }

fun connectUserNamesByCommonEmails(
    names: Set<String>,
    emails: Set<String>,
) : MutableMap<String, MutableSet<String>>
{
    val disconnected = names.toMutableSet()
    val connected = mutableMapOf(
        disconnected.popRandom() to mutableSetOf<String>()
    )
    val usedEmails = mutableSetOf<String>()
    while (disconnected.isNotEmpty()) {
        val email = emails.random().also { usedEmails += it }
        connected.values.random() += email
        connected.getOrPut(disconnected.popRandom()) { mutableSetOf() } += email
    }
    val unusedEmails = emails - usedEmails
    unusedEmails.forEach { email ->
        connected[names.random()]!! += email
    }
    return connected
}