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

package org.apache.pekko.http.scaladsl.server
package directives

import scala.collection.immutable
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import org.apache.pekko
import pekko.http.scaladsl.common._
import pekko.http.impl.util._

/**
 * @groupname param Parameter directives
 * @groupprio param 150
 */
trait ParameterDirectives extends ParameterDirectivesInstances with ToNameReceptacleEnhancements {
  import ParameterDirectives._

  /**
   * Extracts the request's query parameters as a `Map[String, String]`.
   *
   * @group param
   */
  def parameterMap: Directive1[Map[String, String]] = _parameterMap

  /**
   * Extracts the request's query parameters as a `Map[String, List[String]]`.
   *
   * @group param
   */
  def parameterMultiMap: Directive1[Map[String, List[String]]] = _parameterMultiMap

  /**
   * Extracts the request's query parameters as a `Seq[(String, String)]`.
   *
   * @group param
   */
  def parameterSeq: Directive1[immutable.Seq[(String, String)]] = _parameterSeq

  /**
   * Extracts a query parameter value from the request.
   * Rejects the request if the defined query parameter matcher(s) don't match.
   *
   * @group param
   */
  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  private[http] def parameter(pdm: ParamMagnet): pdm.Out = pdm()

  /**
   * Extracts a number of query parameter values from the request.
   * Rejects the request if the defined query parameter matcher(s) don't match.
   *
   * @group param
   */
  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  private[http] def parameters(pdm: ParamMagnet): pdm.Out = pdm()
}

object ParameterDirectives extends ParameterDirectives {
  import BasicDirectives._

  private val _parameterMap: Directive1[Map[String, String]] =
    extract(_.request.uri.query().toMap)

  private val _parameterMultiMap: Directive1[Map[String, List[String]]] =
    extract(_.request.uri.query().toMultiMap)

  private val _parameterSeq: Directive1[immutable.Seq[(String, String)]] =
    extract(_.request.uri.query().toSeq)

  trait ParamSpec {
    type Out
    def get: Directive1[Out]
  }
  object ParamSpec {
    type Aux[T] = ParamSpec { type Out = T }
    def apply[T](directive: Directive1[T]): Aux[T] =
      new ParamSpec {
        type Out = T
        override def get: Directive1[T] = directive
      }

    import Impl._
    import pekko.http.scaladsl.unmarshalling.{ FromStringUnmarshaller => FSU }

    // regular
    implicit def forString(value: String)(implicit fsu: FSU[String]): ParamSpec.Aux[String] = forName(value, fsu)
    implicit def forSymbol(symbol: Symbol)(implicit fsu: FSU[String]): ParamSpec.Aux[String] = forName(symbol.name, fsu)
    implicit def forNR[T](nr: NameReceptacle[T])(implicit fsu: FSU[T]): ParamSpec.Aux[T] = forName(nr.name, fsu)
    implicit def forNUR[T](nur: NameUnmarshallerReceptacle[T]): ParamSpec.Aux[T] = forName(nur.name, nur.um)
    implicit def forNOR[T](nor: NameOptionReceptacle[T])(implicit fsou: FSOU[T]): ParamSpec.Aux[Option[T]] =
      forName(nor.name, fsou)
    implicit def forNDR[T](ndr: NameDefaultReceptacle[T])(implicit fsou: FSOU[T]): ParamSpec.Aux[T] =
      forName(ndr.name, fsou.withDefaultValue(ndr.default))
    implicit def forNOUR[T](nour: NameOptionUnmarshallerReceptacle[T]): ParamSpec.Aux[Option[T]] =
      forName(nour.name, nour.um: FSOU[T])
    implicit def forNDUR[T](ndur: NameDefaultUnmarshallerReceptacle[T]): ParamSpec.Aux[T] =
      forName(ndur.name, (ndur.um: FSOU[T]).withDefaultValue(ndur.default))

    // repeated
    implicit def forRepVR[T](rvr: RepeatedValueReceptacle[T])(implicit fsu: FSU[T]): ParamSpec.Aux[Iterable[T]] =
      forNameRepeated(rvr.name, fsu)
    implicit def forRepVUR[T](rvur: RepeatedValueUnmarshallerReceptacle[T]): ParamSpec.Aux[Iterable[T]] =
      forNameRepeated(rvur.name, rvur.um)

    // required
    implicit def forRVR[T](rvr: RequiredValueReceptacle[T])(implicit fsu: FSU[T]): ParamSpec.Aux[Unit] =
      forNameRequired(rvr.name, fsu, rvr.requiredValue)
    implicit def forRVUR[T](rvur: RequiredValueUnmarshallerReceptacle[T]): ParamSpec.Aux[Unit] =
      forNameRequired(rvur.name, rvur.um, rvur.requiredValue)

    private def forName[T](name: String, fsu: FSOU[T]): ParamSpec.Aux[T] = ParamSpec(filter(name, fsu))
    private def forNameRepeated[T](name: String, fsu: FSU[T]): ParamSpec.Aux[Iterable[T]] =
      ParamSpec(repeatedFilter(name, fsu))
    private def forNameRequired[T](name: String, fsu: FSU[T], requiredValue: T): ParamSpec.Aux[Unit] =
      ParamSpec(requiredFilter(name, fsu, requiredValue).tmap(_ => Tuple1(())))
  }

  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  sealed trait ParamMagnet {
    type Out
    def apply(): Out
  }
  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  object ParamMagnet {
    def apply[T](value: T)(implicit pdef: ParamDef[T]): ParamMagnet { type Out = pdef.Out } =
      new ParamMagnet {
        type Out = pdef.Out
        def apply() = pdef(value)
      }
  }

  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  type ParamDefAux[T, U] = ParamDef[T] { type Out = U }
  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  sealed trait ParamDef[T] {
    type Out
    def apply(value: T): Out
  }
  @deprecated("Use new `parameters` overloads with ParamSpec parameters. Kept for binary compatibility",
    "Akka HTTP 10.2.0")
  object ParamDef {
    import Impl._
    import pekko.http.scaladsl.unmarshalling.{ FromStringUnmarshaller => FSU }

    def paramDef[A, B](f: A => B): ParamDefAux[A, B] =
      new ParamDef[A] {
        type Out = B
        def apply(value: A) = f(value)
      }
    def extractParameter[A, B](f: A => Directive1[B]): ParamDefAux[A, Directive1[B]] = paramDef(f)

    def forString(implicit fsu: FSU[String]): ParamDefAux[String, Directive1[String]] =
      extractParameter[String, String] { string => filter(string, fsu) }
    def forSymbol(implicit fsu: FSU[String]): ParamDefAux[Symbol, Directive1[String]] =
      extractParameter[Symbol, String] { symbol => filter(symbol.name, fsu) }
    def forNR[T](implicit fsu: FSU[T]): ParamDefAux[NameReceptacle[T], Directive1[T]] =
      extractParameter[NameReceptacle[T], T] { nr => filter(nr.name, fsu) }
    def forNUR[T]: ParamDefAux[NameUnmarshallerReceptacle[T], Directive1[T]] =
      extractParameter[NameUnmarshallerReceptacle[T], T] { nr => filter(nr.name, nr.um) }
    def forNOR[T](implicit fsou: FSOU[T]): ParamDefAux[NameOptionReceptacle[T], Directive1[Option[T]]] =
      extractParameter[NameOptionReceptacle[T], Option[T]] { nr => filter[Option[T]](nr.name, fsou) }
    def forNDR[T](implicit fsou: FSOU[T]): ParamDefAux[NameDefaultReceptacle[T], Directive1[T]] =
      extractParameter[NameDefaultReceptacle[T], T] { nr => filter[T](nr.name, fsou.withDefaultValue(nr.default)) }
    def forNOUR[T]: ParamDefAux[NameOptionUnmarshallerReceptacle[T], Directive1[Option[T]]] =
      extractParameter[NameOptionUnmarshallerReceptacle[T], Option[T]] { nr => filter(nr.name, nr.um: FSOU[T]) }
    def forNDUR[T]: ParamDefAux[NameDefaultUnmarshallerReceptacle[T], Directive1[T]] =
      extractParameter[NameDefaultUnmarshallerReceptacle[T], T] { nr =>
        filter[T](nr.name, (nr.um: FSOU[T]).withDefaultValue(nr.default))
      }

    //////////////////// required parameter support ////////////////////

    def forRVR[T](implicit fsu: FSU[T]): ParamDefAux[RequiredValueReceptacle[T], Directive0] =
      paramDef[RequiredValueReceptacle[T], Directive0] { rvr => requiredFilter(rvr.name, fsu, rvr.requiredValue) }
    def forRVDR[T]: ParamDefAux[RequiredValueUnmarshallerReceptacle[T], Directive0] =
      paramDef[RequiredValueUnmarshallerReceptacle[T], Directive0] { rvr =>
        requiredFilter(rvr.name, rvr.um, rvr.requiredValue)
      }

    //////////////////// repeated parameter support ////////////////////

    def forRepVR[T](implicit fsu: FSU[T]): ParamDefAux[RepeatedValueReceptacle[T], Directive1[Iterable[T]]] =
      extractParameter[RepeatedValueReceptacle[T], Iterable[T]] { rvr => repeatedFilter(rvr.name, fsu) }
    def forRepVDR[T]: ParamDefAux[RepeatedValueUnmarshallerReceptacle[T], Directive1[Iterable[T]]] =
      extractParameter[RepeatedValueUnmarshallerReceptacle[T], Iterable[T]] { rvr => repeatedFilter(rvr.name, rvr.um) }

    //////////////////// tuple support ////////////////////

    import pekko.http.scaladsl.server.util.TupleOps._
    import pekko.http.scaladsl.server.util.BinaryPolyFunc

    // not implicit any more
    private[http] def forTuple[T](
        implicit fold: FoldLeft[Directive0, T, ConvertParamDefAndConcatenate.type]): ParamDefAux[T, fold.Out] =
      paramDef[T, fold.Out](fold(BasicDirectives.pass, _))

    object ConvertParamDefAndConcatenate extends BinaryPolyFunc {
      implicit def from[P, TA, TB](implicit pdef: ParamDef[P] { type Out = Directive[TB] }, ev: Join[TA, TB])
          : BinaryPolyFunc.Case[Directive[TA], P, ConvertParamDefAndConcatenate.type] { type Out = Directive[ev.Out] } =
        at[Directive[TA], P] { (a, t) => a & pdef(t) }
    }
  }

  /** Actual directive implementations shared between old and new API */
  private object Impl {
    import BasicDirectives._
    import RouteDirectives._
    import FutureDirectives._
    import pekko.http.scaladsl.unmarshalling.{ FromStringUnmarshaller => FSU, _ }
    type FSOU[T] = Unmarshaller[Option[String], T]

    def filter[T](paramName: String, fsou: FSOU[T]): Directive1[T] =
      extractRequestContext.flatMap { ctx =>
        import ctx.executionContext
        import ctx.materializer
        Try(ctx.request.uri.query()) match {
          case Success(query) => handleParamResult(paramName, fsou(query.get(paramName)))
          case Failure(t)     => reject(MalformedRequestContentRejection("The request's query string is invalid.", t))
        }
      }

    def requiredFilter[T](paramName: String, fsou: FSOU[T], requiredValue: Any): Directive0 =
      extractRequestContext.flatMap { ctx =>
        import ctx.executionContext
        import ctx.materializer
        onComplete(fsou(ctx.request.uri.query().get(paramName))).flatMap {
          case Success(value) if value == requiredValue => pass
          case Success(value) =>
            reject(InvalidRequiredValueForQueryParamRejection(paramName, requiredValue.toString, value.toString))
          case _ => reject(MissingQueryParamRejection(paramName))
        }
      }

    def repeatedFilter[T](paramName: String, fsu: FSU[T]): Directive1[Iterable[T]] =
      extractRequestContext.flatMap { ctx =>
        import ctx.executionContext
        import ctx.materializer
        handleParamResult(paramName, Future.sequence(ctx.request.uri.query().getAll(paramName).map(fsu.apply)))
      }

    def handleParamResult[T](paramName: String, result: Future[T]): Directive1[T] =
      onComplete(result).flatMap {
        case Success(x)                               => provide(x)
        case Failure(Unmarshaller.NoContentException) => reject(MissingQueryParamRejection(paramName))
        case Failure(x)                               => reject(MalformedQueryParamRejection(paramName, x.getMessage.nullAsEmpty, Option(x.getCause)))
      }
  }
}
