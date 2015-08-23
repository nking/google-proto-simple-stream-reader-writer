**A Java and Javascript library to read and write Google Protocol Buffer messages using delimiters between messages to aid message-by-message rendering.**

Also includes a demo java web application that uses the main project libraries to stream protocol buffer
messages and includes example java and javascript clients to read the messages
http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/

```
To build:
   mvn clean package
The binary will be in target/gpb-stream-reader-writer-{version}.jar

Or include the dependency in your maven2 pom.xml:

<dependency>
  <groupId>com.google.code.google-proto-simple-reader-writer</groupId>
  <artifactId>gpb-stream-reader-writer</artifactId>
  <version>1.5.3</version>
</dependency>


```

**See the [Wiki](http://code.google.com/p/google-proto-simple-stream-reader-writer/wiki/usage).**

**See the [demo project](http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/).**

```

Summary of encoding, content type and additional configuration that work for
passing protocol buffers to javascript clients:

    browsers              char encoding   content type   AJAX Object
-----------------------   -------------   ------------   -----------
Chrome, webkit, mozilla   UTF-8           octet-stream   XMLHttpRequest

IE11                      UTF-8           octet-stream   XMLHttpRequest

IE10                      UTF-8           octet-stream   XDomainRequest
                          UTF-8           text
                          windows-1252    text
** note that IE10 needs <meta http-equiv="X-UA-Compatible" content="IE=9" />

IE9                                                      XDomainRequest or ActiveXObjectRequest
                          UTF-8           octet-stream
                          windows-1252    text

IE8(CORS)                 windows-1252    text           XDomainRequest

IE6, IE7, E8(local)       UTF-8           octet-stream   ActiveXObjectRequest
                          windows-1252    text
** note that vbscript was necessary to read the data as binary from the response body
of ActiveXObjectRequest.
** also note that ActiveXObjectRequest is configured to attempt modes
  "Msxml2.XMLHTTP.6.0","Msxml2.XMLHTTP.4.0",
  "Msxml2.XMLHTTP.3.0", "Msxml2.XMLHTTP", "Microsoft.XMLHTTP"
** If using CORS to share data between client and server of different domains,
   IE browsers > IE7 and < IE10 will need to use XDomainRequest instead of ActiveXObjectRequest.
   the content then has to be text, so use encoding windows-1252.
   did not test on IE6 and IE7 XDomainRequest w/ CORS...


Note that the cross origin resource sharing domain configurations above work for http and 
https, excepting XDomainRequest which does not allow https from an http origin at this time
(though they will hopefully fix the security warning that more secure content may
be present instead of removing ability to use https from their XDomainRequest object...).

The tests are in testGPB.html.

Note too that for javascipt clients, you'll want to scrub your text for characters that might break
your javascript deserialization of the messages.  See the Util.java. Essentially, quotes and line breaks...
        str = str.replaceAll("'", "&#39;");
        str = str.replaceAll("\"", "&#34;");
        str = str.replaceAll("\n", "");
        str = str.replaceAll("\r", ""); // this shouldn't be necessary, but not a bad idea

```