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

Usage is in the [Wiki](http://code.google.com/p/google-proto-simple-stream-reader-writer/wiki/usage).
Demo project is [here](http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/).
The demo project has a
> [README file](http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/README) and an [example](http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/server/src/main/java/com/climbwithyourfeet/services/serveGPB/GPBServlet.java) for streaming the buffers using the Google Protocol Buffer built-in delimiters.

**Example write using custom delimiters from a Java Servlet:**

```
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
throws ServletException, IOException {

        BufferedOutputStream out = null;

        try {

            List<ExampleMsg> messages = getPBMessages(req);

            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");

            if (useProjectDelimiters) {

                int expectedSize = PBWireByteMarkerHelper.estimateTotalContentLength(messages);
                resp.setContentLength(expectedSize);

                out = new BufferedOutputStream(resp.getOutputStream(), 1024);

                PBStreamWriter.writeToStream(out, messages);

                resp.setStatus(200);
            } else {
                // use Google Protocol Buffer 'built-in' delimiters
                out = new BufferedOutputStream(resp.getOutputStream(), 1024);

                for (int i = 0; i < messages.size(); i++) {
                    messages.get(i).writeDelimitedTo(out);
                }

                resp.setStatus(200);
            }

        } finally {
            if (out != null)
                out.close();
        }

```

**Example Java read:**
```
   PBStreamReader<ExampleMsg> pbReader = new PBStreamReader();
   Builder builder = ExampleMsg.newBuilder();
   List<ExampleMsg> messages = pbReader.read(inStream, builder);

Or use a callback:
   List<ExampleMsg> messages = pbReader.read(inStream, builder, callback);
```

**See the unit [tests](http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/server/src/main/webapp/testGPB.html) for working examples too.**
They include an example of receiving the
messages asynchronously as they are de-serialized.

**Javascript library**
```
javascript scripts to help with ajax calls and read messages are provided (along with unit tests in
the demo project to see results by browser). 

In general, the delimiters are useful to you if you are using web workers else, protocol buffers 
without delimiters are probably what you'll want.  There are helper functions for both and they 
could be built upon.


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
http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/javascript/
```
function readMessagesFromUint8Array(uint8Array, createPROTOMessage, perMessageCallback,
 completedCallback, errorCallback, dictionary);

function readMessagesFromBinaryString(binaryString, createPROTOMessage, perMessageCallback,
 completedCallback, errorCallback, dictionary);

function makeXMLHttpRequestForArrayBufferWithTypedArray(url, createPROTOMessage, 
perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMilliseconds);

function makeXMLHttpRequest(url, createPROTOMessage, perMessageCallback, 
completedCallback, errorHandle, userdictionary, timeoutMilliseconds);

function makeSynchronousXMLHttpRequest(url, createPROTOMessage, perMessageCallback,
completedCallback, errorHandle, userdictionary);

function makeXDomainRequest(url, createPROTOMessage, perMessageCallback, 
completedCallback, errorHandle, userdictionary, timeoutMilliseconds);

function makeBinaryActiveXObjectRequest(url, createPROTOMessage, perMessageCallback,
completedCallback, errorHandle, userdictionary, timeoutMilliseconds);

```

**The demo project showing server side and client side usage is available here at:
http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/demo/.**


The maven importable java and javascript clients without demo project are available from the https://oss.sonatype.org nexus maven2 repository (though the latest javascript changes are in google-code vcs here).

Detailed instructions in using Google Protocol Buffers beyond this library:
  * Download http://code.google.com/apis/protocolbuffers/ and install it on your computer.
  * Create a .proto file and compile it.  The Google tutorial has an example .proto file and there's a simple one here included to demonstrate generating the source, compiling, and using it. The example .proto file in this project is at:
> > http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/src/#src%2Ftest%2Fjava%2Fcom%2Fgoogle%2Fcode%2Fproto%2Fmodel


> You can use the maven plugin maven-protoc-plugin configured in the pom.xml and the command with id 'generate-sources' to compile the .proto file to a java source.  The compile plugin finds the generated java source and builds your GPB java binary class.
  * Decide on our delimiters (native to buffers or customized here).  The demo project servlet has a couple of examples.
  * Include this maven plugin in your pom.xml build dependencies.
  * Write your messages. For any string fields, consider whether you need to transform the string for your client.
> > For an html client you may want to add
```
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.0.1</version>
        </dependency>
```
> > and use StringEscapeUtils.escapeHtml4(str) to convert your non-ascii characters into html4 sgml entities.  Alternatively, you can use the junidecode library to transliterate characters into 7-bit ascii (you will loose all accents)  http://sourceforge.net/projects/junidecode/files/.  Note that you may want to limit string lengths to 127 characters for a short time if you are using a javascript client (I'll remove this note after the bug fix).
  * Write your servlets to use PBStreamWriter if using the customized delimiters.
  * Write your java clients to use PBStreamReader if using the customized delimiters and consider using the callback option asynchronously.
  * If you're writing a javascript client:
    * Download https://github.com/sirikata/protojs and build it.
    * Compile your .proto file to a .proto.js file using the sirikata project.
    * Create your html javascript client by including the scripts .proto.js that you just compiled, googleProtocolBufferMessagesReader.js, googleProtocolBufferAjaxHelper.js from this project, and the protobuf.js and pbj.js scripts from the Sirikata project.

[http://code.google.com/p/google-proto-simple-stream-reader-writer/source/browse/javascript/

https://github.com/sirikata/protojs