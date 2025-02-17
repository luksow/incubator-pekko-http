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

import org.apache.pekko
import pekko.http.scaladsl.model.AttributeKeys.webSocketUpgrade
import pekko.http.scaladsl.model.ws.{ Message, UpgradeToWebSocket, WebSocketUpgrade }
import pekko.stream.scaladsl.Flow

/**
 * @groupname websocket WebSocket directives
 * @groupprio websocket 230
 */
trait WebSocketDirectives {
  import AttributeDirectives._
  import BasicDirectives._
  import HeaderDirectives._
  import RouteDirectives._

  /**
   * Extract the [[UpgradeToWebSocket]] header if existent. Rejects with an [[ExpectedWebSocketRequestRejection]], otherwise.
   *
   * @group websocket
   */
  @deprecated("use `extractWebSocketUpgrade` instead", since = "Akka HTTP 10.2.0")
  def extractUpgradeToWebSocket: Directive1[UpgradeToWebSocket] =
    optionalHeaderValueByType(classOf[UpgradeToWebSocket]).flatMap {
      case Some(upgrade) => provide(upgrade)
      case None          => reject(ExpectedWebSocketRequestRejection)
    }

  /**
   * Extract the WebSocketUpgrade attribute if this is a WebSocket request.
   * Rejects with an [[ExpectedWebSocketRequestRejection]], otherwise.
   *
   * @group websocket
   */
  def extractWebSocketUpgrade: Directive1[WebSocketUpgrade] =
    optionalAttribute(webSocketUpgrade).flatMap {
      case Some(upgrade) => provide(upgrade)
      case None          => reject(ExpectedWebSocketRequestRejection)
    }

  /**
   * Extract the list of WebSocket subprotocols as offered by the client in the [[Sec-WebSocket-Protocol]] header if
   * this is a WebSocket request. Rejects with an [[ExpectedWebSocketRequestRejection]], otherwise.
   *
   * @group websocket
   */
  def extractOfferedWsProtocols: Directive1[immutable.Seq[String]] =
    extractWebSocketUpgrade.map(_.requestedProtocols)

  /**
   * Handles WebSocket requests with the given handler and rejects other requests with an
   * [[ExpectedWebSocketRequestRejection]].
   *
   * @group websocket
   */
  def handleWebSocketMessages(handler: Flow[Message, Message, Any]): Route =
    handleWebSocketMessagesForOptionalProtocol(handler, None)

  /**
   * Handles WebSocket requests with the given handler if the given subprotocol is offered in the request and
   * rejects other requests with an [[ExpectedWebSocketRequestRejection]] or an [[UnsupportedWebSocketSubprotocolRejection]].
   *
   * @group websocket
   */
  def handleWebSocketMessagesForProtocol(handler: Flow[Message, Message, Any], subprotocol: String): Route =
    handleWebSocketMessagesForOptionalProtocol(handler, Some(subprotocol))

  /**
   * Handles WebSocket requests with the given handler and rejects other requests with an
   * [[ExpectedWebSocketRequestRejection]].
   *
   * If the `subprotocol` parameter is None any WebSocket request is accepted. If the `subprotocol` parameter is
   * `Some(protocol)` a WebSocket request is only accepted if the list of subprotocols supported by the client (as
   * announced in the WebSocket request) contains `protocol`. If the client did not offer the protocol in question
   * the request is rejected with an [[UnsupportedWebSocketSubprotocolRejection]] rejection.
   *
   * To support several subprotocols you may chain several `handleWebSocketMessagesForOptionalProtocol` routes.
   *
   * @group websocket
   */
  def handleWebSocketMessagesForOptionalProtocol(
      handler: Flow[Message, Message, Any], subprotocol: Option[String]): Route =
    extractWebSocketUpgrade { upgrade =>
      if (subprotocol.forall(sub => upgrade.requestedProtocols.exists(_.equalsIgnoreCase(sub))))
        complete(upgrade.handleMessages(handler, subprotocol))
      else
        reject(UnsupportedWebSocketSubprotocolRejection(subprotocol.get)) // None.forall == true
    }
}
