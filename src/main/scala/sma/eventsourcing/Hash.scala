package sma.eventsourcing

object Hash {
  def sha256(text: String) : String = String.format("%064x", new java.math.BigInteger(1, java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes("UTF-8"))))
  def sha256(seq: Seq[String]) : String = sha256(seq.mkString(", "))
}
