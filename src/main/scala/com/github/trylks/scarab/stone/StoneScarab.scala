package com.github.trylks.scarab.stone

import com.github.trylks.scarab.Scarab
import com.github.trylks.scarab.LivingScarab

trait StoneScarab extends Scarab {
  def apply(sc:LivingScarab) = {
    sc.apply()
  }
}