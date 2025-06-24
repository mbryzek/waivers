package helpers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneServerPerSuite}

trait DefaultSpec extends PlaySpec with Helpers

trait DefaultAppSpec extends DefaultSpec with GuiceOneAppPerSuite

trait DefaultServerSpec extends DefaultSpec with GuiceOneServerPerSuite