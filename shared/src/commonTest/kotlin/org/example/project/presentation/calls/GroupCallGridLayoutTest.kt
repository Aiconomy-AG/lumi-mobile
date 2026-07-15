package org.example.project.presentation.calls

import kotlin.test.Test
import kotlin.test.assertEquals

class GroupCallGridLayoutTest {
    @Test
    fun singleParticipantUsesOneColumn() {
        assertEquals(1, groupCallGridColumns(1))
    }

    @Test
    fun twoToFourParticipantsUseTwoColumns() {
        assertEquals(2, groupCallGridColumns(2))
        assertEquals(2, groupCallGridColumns(4))
    }

    @Test
    fun fiveOrMoreParticipantsUseThreeColumns() {
        assertEquals(3, groupCallGridColumns(5))
        assertEquals(3, groupCallGridColumns(9))
    }
}
