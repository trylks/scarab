package com.github.trylks.scarab.paper

// import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import scala.concurrent.duration._
import java.util.Locale
import com.github.trylks.scarab.Scarab


trait SandScarab extends Scarab{
  // def now = Calendar.getInstance().getTime()
  def now = new Date()
  def mailTime(time:Date=new Date()):String = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).format(time)
  def agoBy(days:Int=0, hours:Int=0, minutes:Int=0):Date = new Date(new Date().getTime() - days.days.toMillis - hours.hours.toMillis - minutes.minutes.toMillis)
  def mailAgoBy(days:Int=0, hours:Int=0, minutes:Int=0):String = mailTime(agoBy(days, hours, minutes))
  def parseMailTime(time:String):Date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).parse(time)
}