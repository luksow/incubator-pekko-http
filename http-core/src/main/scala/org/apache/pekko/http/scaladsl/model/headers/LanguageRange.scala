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

import scala.language.implicitConversions
import scala.collection.immutable
import org.apache.pekko
import pekko.http.impl.util._
import pekko.http.scaladsl.model.WithQValue
import pekko.http.javadsl.{ model => jm }
import pekko.http.impl.util.JavaMapping.Implicits._
import pekko.http.ccompat.{ pre213, since213 }

sealed trait LanguageRange extends jm.headers.LanguageRange with ValueRenderable with WithQValue[LanguageRange] {
  def qValue: Float
  def primaryTag: String
  def subTags: immutable.Seq[String]
  def matches(lang: Language): Boolean
  final def render[R <: Rendering](r: R): r.type = {
    r ~~ primaryTag
    if (subTags.nonEmpty) subTags.foreach(r ~~ '-' ~~ _)
    if (qValue < 1.0f) r ~~ ";q=" ~~ qValue
    r
  }

  /** Java API */
  def matches(language: jm.headers.Language) = matches(language.asScala)
  def getSubTags: java.lang.Iterable[String] = subTags.asJava
}
object LanguageRange {
  case class `*`(qValue: Float) extends LanguageRange {
    require(0.0f <= qValue && qValue <= 1.0f, "qValue must be >= 0 and <= 1.0")
    def primaryTag = "*"
    def subTags = Nil
    def matches(lang: Language) = true
    def withQValue(qValue: Float) =
      if (qValue == 1.0f) `*` else if (qValue != this.qValue) `*`(qValue.toFloat) else this
  }
  object `*` extends `*`(1.0f)

  final case class One(language: Language, qValue: Float) extends LanguageRange {
    require(0.0f <= qValue && qValue <= 1.0f, "qValue must be >= 0 and <= 1.0")
    def matches(l: Language) =
      (language.primaryTag.equalsIgnoreCase(l.primaryTag)) &&
      language.subTags.size <= l.subTags.size &&
      language.subTags.zip(l.subTags).forall(t => t._1.equalsIgnoreCase(t._2))
    def primaryTag = language.primaryTag
    def subTags = language.subTags
    def withQValue(qValue: Float) = One(language, qValue)
  }

  implicit def apply(language: Language): LanguageRange = apply(language, 1.0f)
  def apply(language: Language, qValue: Float): LanguageRange = One(language, qValue)
}

final case class Language(primaryTag: String, subTags: immutable.Seq[String])
    extends jm.headers.Language with ValueRenderable with WithQValue[LanguageRange] {
  def withQValue(qValue: Float) = LanguageRange(this, qValue.toFloat)
  def render[R <: Rendering](r: R): r.type = {
    r ~~ primaryTag
    if (subTags.nonEmpty) subTags.foreach(r ~~ '-' ~~ _)
    r
  }

  /** Java API */
  def getSubTags: java.lang.Iterable[String] = subTags.asJava
}
object Language {
  implicit def apply(compoundTag: String): Language =
    if (compoundTag.indexOf('-') >= 0) {
      val tags = compoundTag.split('-')
      new Language(tags.head, immutable.Seq(tags.tail: _*))
    } else new Language(compoundTag, immutable.Seq.empty)
  @pre213
  def apply(primaryTag: String, subTags: String*): Language =
    new Language(primaryTag, immutable.Seq(subTags: _*))
  @since213
  def apply(primaryTag: String, firstSubTag: String, otherSubTags: String*): Language =
    new Language(primaryTag, firstSubTag +: otherSubTags)

}
