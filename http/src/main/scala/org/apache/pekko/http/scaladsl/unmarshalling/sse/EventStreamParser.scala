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

package org.apache.pekko.http
package scaladsl
package unmarshalling
package sse

import org.apache.pekko
import pekko.NotUsed
import pekko.annotation.ApiMayChange
import pekko.http.scaladsl.model.sse.ServerSentEvent
import pekko.stream.scaladsl.Flow
import pekko.util.ByteString

/**
 * Flow that converts raw byte string input into [[ServerSentEvent]]s.
 *
 * This API is made for use in non-akka-http clients, like Play's WSClient.
 */
@ApiMayChange
object EventStreamParser {

  /**
   * Flow that converts raw byte string input into [[ServerSentEvent]]s.
   *
   * This API is made for use in non-akka-http clients, like Play's WSClient.
   *
   * @param maxLineSize The maximum size of a line for the event Stream parser
   * @param maxEventSize The maximum size of a server-sent event for the event Stream parser
   */
  def apply(maxLineSize: Int, maxEventSize: Int): Flow[ByteString, ServerSentEvent, NotUsed] =
    apply(maxLineSize, maxEventSize, emitEmptyEvents = false)

  /**
   * Flow that converts raw byte string input into [[ServerSentEvent]]s.
   *
   * This API is made for use in non-akka-http clients, like Play's WSClient.
   *
   * @param maxLineSize The maximum size of a line for the event Stream parser
   * @param maxEventSize The maximum size of a server-sent event for the event Stream parser
   * @param emitEmptyEvents Should the parser emit events with empty data field
   */
  def apply(maxLineSize: Int, maxEventSize: Int, emitEmptyEvents: Boolean): Flow[ByteString, ServerSentEvent, NotUsed] =
    Flow[ByteString].via(new LineParser(maxLineSize)).via(new ServerSentEventParser(maxEventSize, emitEmptyEvents))
}
