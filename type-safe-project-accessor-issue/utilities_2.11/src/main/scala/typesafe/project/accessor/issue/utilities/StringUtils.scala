package typesafe.project.accessor.issue.utilities

import typesafe.project.accessor.issue.list.typesafe.project.accessor.issue.list.LinkedList

object StringUtils {
  def join(source: LinkedList): String = {
    JoinUtils.join(source)
  }

  def split(source: String): LinkedList = {
    SplitUtils.split(source)
  }
}
