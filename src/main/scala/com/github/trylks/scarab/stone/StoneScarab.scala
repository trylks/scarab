package com.github.trylks.scarab.stone

import com.github.trylks.scarab.Scarab

trait StoneScarab extends Scarab {
  def apply(sc:Scarab){
    sc.apply()
  }
}