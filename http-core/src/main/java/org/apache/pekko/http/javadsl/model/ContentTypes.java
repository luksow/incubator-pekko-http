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

package org.apache.pekko.http.javadsl.model;


import org.apache.pekko.http.scaladsl.model.ContentType$;

/**
 * Contains the set of predefined content-types for convenience.
 * <p>
 * If the {@link ContentType} you're looking for is not pre-defined here,
 * you can obtain it from a {@link MediaType} by using: {@code MediaTypes.TEXT_HTML.toContentType()}
 */
public final class ContentTypes {
  private ContentTypes() { }

  public static final ContentType.WithFixedCharset APPLICATION_JSON = MediaTypes.APPLICATION_JSON.toContentType();
  public static final ContentType.Binary APPLICATION_OCTET_STREAM = MediaTypes.APPLICATION_OCTET_STREAM.toContentType();

  public static final ContentType.WithCharset TEXT_PLAIN_UTF8 =
          org.apache.pekko.http.scaladsl.model.ContentTypes.text$divplain$u0028UTF$minus8$u0029();
  public static final ContentType.WithCharset TEXT_HTML_UTF8 =
          org.apache.pekko.http.scaladsl.model.ContentTypes.text$divhtml$u0028UTF$minus8$u0029();
  public static final ContentType.WithCharset TEXT_XML_UTF8 =
          org.apache.pekko.http.scaladsl.model.ContentTypes.text$divxml$u0028UTF$minus8$u0029();

  public static final ContentType.WithCharset TEXT_CSV_UTF8 =
          org.apache.pekko.http.scaladsl.model.ContentTypes.text$divcsv$u0028UTF$minus8$u0029();

  public static final ContentType.Binary APPLICATION_GRPC_PROTO = MediaTypes.APPLICATION_GRPC_PROTO.toContentType();
  public static final ContentType.WithFixedCharset APPLICATION_X_WWW_FORM_URLENCODED =
      org.apache.pekko.http.scaladsl.model.ContentTypes.application$divx$minuswww$minusform$minusurlencoded();

  public static final ContentType.Binary NO_CONTENT_TYPE =
          org.apache.pekko.http.scaladsl.model.ContentTypes.NoContentType();

  public static ContentType parse(String contentType) {
    return ContentType$.MODULE$.parse(contentType).right().get();
  }

  public static ContentType.Binary create(MediaType.Binary mediaType) {
    return ContentType$.MODULE$.apply((org.apache.pekko.http.scaladsl.model.MediaType.Binary) mediaType);
  }

  public static ContentType.WithFixedCharset create(MediaType.WithFixedCharset mediaType) {
    return ContentType$.MODULE$.apply((org.apache.pekko.http.scaladsl.model.MediaType.WithFixedCharset) mediaType);
  }

  public static ContentType.WithCharset create(MediaType.WithOpenCharset mediaType, HttpCharset charset) {
    return ContentType$.MODULE$.apply((org.apache.pekko.http.scaladsl.model.MediaType.WithOpenCharset) mediaType,
            (org.apache.pekko.http.scaladsl.model.HttpCharset) charset);
  }
}
