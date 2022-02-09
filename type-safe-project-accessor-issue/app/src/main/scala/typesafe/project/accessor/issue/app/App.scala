package typesafe.project.accessor.issue.app

import org.apache.commons.text.WordUtils
import typesafe.project.accessor.issue.utilities.StringUtils

object App {
  def main(args: Array[String]): Unit = {
    val tokens = StringUtils.split(MessageUtils.getMessage())
    val result = StringUtils.join(tokens)
    println(WordUtils.capitalize(result))
  }
}
