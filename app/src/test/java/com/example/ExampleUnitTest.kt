package com.example

import com.example.data.FlowContentRepository
import com.example.model.BufferOverflowType
import com.example.model.FlowKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testFlowContentRepositoryData() {
    assertTrue("Should have presets", FlowContentRepository.presets.isNotEmpty())
    assertTrue("Should have sound-alikes", FlowContentRepository.soundAlikes.isNotEmpty())
    assertTrue("Should have flashcards", FlowContentRepository.flashcards.isNotEmpty())
    assertTrue("Should have quiz questions", FlowContentRepository.quizQuestions.isNotEmpty())

    // Verify all quiz questions have valid correctIndex
    FlowContentRepository.quizQuestions.forEach { q ->
      assertTrue("Option count >= 2", q.options.size >= 2)
      assertTrue("correctIndex within bounds", q.correctIndex in q.options.indices)
      assertNotNull("Explanation present", q.explanation)
    }

    // Verify tryEmit trap preset exists
    val tryEmitTrap = FlowContentRepository.presets.find { it.id == "preset_tryemit_trap" }
    assertNotNull(tryEmitTrap)
    assertEquals(FlowKind.SHARED_FLOW, tryEmitTrap?.flowKind)
    assertEquals(0, tryEmitTrap?.replay)
    assertEquals(0, tryEmitTrap?.extraBuffer)
    assertEquals(BufferOverflowType.SUSPEND, tryEmitTrap?.overflow)
  }
}
