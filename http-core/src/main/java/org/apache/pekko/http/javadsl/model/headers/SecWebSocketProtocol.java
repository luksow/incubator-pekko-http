/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2016-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.http.javadsl.model.headers;

import org.apache.pekko.http.impl.util.Util;

/**
 *  Model for the `Sec-WebSocket-Protocol` header.
 */
public abstract class SecWebSocketProtocol extends org.apache.pekko.http.scaladsl.model.HttpHeader {
  public abstract Iterable<String> getProtocols();

  public static SecWebSocketProtocol create(String... protocols) {
    return new org.apache.pekko.http.scaladsl.model.headers.Sec$minusWebSocket$minusProtocol(Util.convertArray(protocols));
  }

}

