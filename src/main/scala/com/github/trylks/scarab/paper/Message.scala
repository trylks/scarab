package com.github.trylks.scarab.paper

import scala.reflect.io.File
import scala.collection.mutable.ArrayBuffer

class Message(var to:String, var from:String, var subject:String, var body:String, attachments:ArrayBuffer[File]) {
  
  def to_ (address:String):Message = {
    this.to = address
    return this
  }
  
  def from_ (address:String): Message = {
    this.from = address
    this
  }
  
  def subject_ (text:String): Message = {
    this.subject = text
    this
  }
  
  def body_ (text:String): Message = {
    this.body = text
    this
  }
  
  def attach(file:File): Message = {
    this.attachments += file
    this
  }


}