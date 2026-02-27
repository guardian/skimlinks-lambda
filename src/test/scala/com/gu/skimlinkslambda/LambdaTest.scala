package com.gu.skimlinkslambda

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LambdaTest extends AnyFlatSpec with Matchers {

  "hasExcessiveDropOff" should "return true when drop is more than 20%" in {
    Lambda.hasExcessiveDropOff(42000, 20000) shouldBe true
  }

  it should "return false when drop is exactly 20%" in {
    Lambda.hasExcessiveDropOff(42000, 33600) shouldBe false
  }

  it should "return false when drop is less than 20%" in {
    Lambda.hasExcessiveDropOff(42000, 40000) shouldBe false
  }

  it should "return false when count increases" in {
    Lambda.hasExcessiveDropOff(42000, 45000) shouldBe false
  }

  it should "return false when count stays the same" in {
    Lambda.hasExcessiveDropOff(42000, 42000) shouldBe false
  }

  it should "return false when previous count is zero" in {
    Lambda.hasExcessiveDropOff(0, 42000) shouldBe false
  }

  it should "return true when new count is zero and previous was non-zero" in {
    Lambda.hasExcessiveDropOff(42000, 0) shouldBe true
  }
}
