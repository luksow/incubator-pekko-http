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

package org.apache.pekko.http.javadsl.model.headers;

/**
 *  Model for the `Transfer-Encoding` header.
 *  Specification: http://tools.ietf.org/html/draft-ietf-httpbis-p1-messaging-26#section-3.3.1
 */
public abstract class TE extends org.apache.pekko.http.scaladsl.model.HttpHeader {
    public abstract Iterable<org.apache.pekko.http.javadsl.model.TransferEncoding> getAcceptableEncodings();

    public static TE create(org.apache.pekko.http.javadsl.model.TransferEncoding... acceptableEncodings) {
        return new org.apache.pekko.http.scaladsl.model.headers.TE(org.apache.pekko.http.impl.util.Util.<org.apache.pekko.http.javadsl.model.TransferEncoding, org.apache.pekko.http.scaladsl.model.TransferEncoding>convertArray(acceptableEncodings));
    }
}
