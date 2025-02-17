/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.http.scaladsl.model.headers

import org.apache.pekko
import pekko.http.impl.model.parser.UriParser
import pekko.http.impl.util.JavaMapping.Implicits._
import pekko.http.impl.util._
import pekko.http.javadsl.{ model => jm }
import pekko.http.scaladsl.model.Uri
import org.parboiled2.UTF8

import scala.collection.immutable
import scala.language.implicitConversions

abstract class HttpOriginRange extends jm.headers.HttpOriginRange with ValueRenderable {
  def matches(origin: HttpOrigin): Boolean

  /** Java API */
  def matches(origin: jm.headers.HttpOrigin): Boolean = matches(origin.asScala)
}

object HttpOriginRange {
  case object `*` extends HttpOriginRange {
    def matches(origin: HttpOrigin) = true
    def render[R <: Rendering](r: R): r.type = r ~~ '*'
  }

  def apply(origins: HttpOrigin*): Default = Default(immutable.Seq(origins: _*))

  final case class Default(origins: immutable.Seq[HttpOrigin]) extends HttpOriginRange {
    def matches(origin: HttpOrigin): Boolean = origins contains origin
    def render[R <: Rendering](r: R): r.type = r ~~ origins
  }

}

final case class HttpOrigin(scheme: String, host: Host) extends jm.headers.HttpOrigin with ValueRenderable {
  def render[R <: Rendering](r: R): r.type = host.renderValue(r ~~ scheme ~~ "://")
}

object HttpOrigin {
  implicit val originsRenderer: Renderer[immutable.Seq[HttpOrigin]] = Renderer.seqRenderer(" ", "null")

  implicit def apply(str: String): HttpOrigin = {
    val parser = new UriParser(str, UTF8, Uri.ParsingMode.Relaxed)
    parser.parseOrigin()
  }
}
