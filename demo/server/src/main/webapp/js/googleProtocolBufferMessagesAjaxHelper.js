/*
* Methods to help w/ browser specific ajax calls and parsing of responses.
*
* A few things found:
*
*    -- For IE6-IE7 this combination works:
*       server settings:
*            content-type=octet/stream
*            character encoding=UTF-8
*       Google protocol buffers written to stream using the message's .writeDelimitedTo(out)
*       and client AJAX being ActiveXObject with attempts for "Msxml2.XMLHTTP.6.0","Msxml2.XMLHTTP.4.0",
*           "Msxml2.XMLHTTP.3.0", "Msxml2.XMLHTTP", "Microsoft.XMLHTTP"
*            Note, that vbscript was necessary to read the data as binary from the response body.
*            see testGPB.html
*
*    -- For IE8 this combination works:
*       server settings:
*            content-type=text/plain
*            character encoding=windows-1252
*       Google protocol buffers written to stream using the message's .writeDelimitedTo(out)
*       and client code should use AJAX  XDomainRequest
*
*    -- For IE9, this combination works:
*       server settings:
*            content-type=octet/stream
*            character encoding=UTF-7
*       google protocol buffers written to stream using the message's .writeDelimitedTo(out)
*       and client AJAX being XDomainRequest
*
*    -- For webkit (Chrome, Safari), nearly all combinations work, but this complements the above:
*       server settings:
*            content-type=octet/stream
*            character encoding=UTF-8
*       google protocol buffers written to stream using the message's .writeDelimitedTo(out)
*       and client being XMLHttpRequest
*
*   -- For mozilla, this combination works:
*       server settings:
*            content-type=octet/stream
*            character encoding=UTF-8
*       google protocol buffers written to stream using the message's .writeDelimitedTo(out)
*       and client being XMLHttpRequest (but don't use arrayBuffer setting)
*
* The generated messages are Google Protocol Buffer messages, whose templates
* were compiled from the Google Protocol Buffer library
* http://code.google.com/apis/protocolbuffers/
*
* The messages can be sent over the network separated by a byte marker delimiter.
* The library that writes the delimiters and the messages to the network stream is
*    http://code.google.com/p/google-proto-simple-stream-reader-writer/
*
* This script reads the binary message delimeters
* and then uses
* protobuf.js https://github.com/sirikata/protojs/blob/master/protobuf.js
* and pbj.js https://github.com/sirikata/protojs/blob/master/pbj.js
* to deserialize the messages.
*
* Note that the ActiveXObject function below needs this bit of vbscript added to your html header:

        <!-- from http://miskun.com/javascript/internet-explorer-and-binary-files-data-access/ -->
        <!--[if IE]>
            <script type="text/vbscript">
                Function IEBinaryToArray_ByteStr(Binary)
                    IEBinaryToArray_ByteStr = CStr(Binary)
                End Function
                Function IEBinaryToArray_ByteStr_Last(Binary)
                    Dim lastIndex
                    lastIndex = LenB(Binary)
                    if lastIndex mod 2 Then
                        IEBinaryToArray_ByteStr_Last = Chr( AscB( MidB( Binary, lastIndex, 1 ) ) )
                    Else
                        IEBinaryToArray_ByteStr_Last = ""
                    End If
                End Function
            </script>

        <![endif]-->
*/

/**
   For the functions below, the callback arguments need these forms.  See testGPB.html.

    var successCallback = function(messages) {
    }
    var errorCallback = function(errorTextMessage) {
    }
    var createYourPROTOMessage2 = function() {
        return new your.proto.package.YourMessage;
    };

    var perMessageCallback = function renderEvent(gpbMessage, dictionary) {
        your code
    };
    var completedCallback = function deserializationIsDone(dictionary) {
        your code
    };
    var errorHandle = function(errorString, dictionary) {
        your code
    };
    var userdictionary = {'key1': "value1"};
*/

function makeXMLHttpRequestForArrayBufferWithTypedArray(url, createPROTOMessage, 
    perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
        
    var useArrayBuffer = true;
    var typedArrayCallback = function(response) {
        try {
            var msgUInt8Array = new Uint8Array(response);
            readTypedArrayMessagesWithGPBDelimiters(msgUInt8Array, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
        } catch (e) {
            errorHandle(e, userdictionary);
        }
    }
    var errCallback = function(msg) {
        errorHandle(msg, userdictionary);
    }
    _makeXMLHttpRequest(url, typedArrayCallback, errCallback, useArrayBuffer, timeoutMillis);
}
function makeXMLHttpRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var useArrayBuffer = false;
    var stringCallback = function(responseText) {
        readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    var errCallback = function(msg) {
        errorHandle(msg, userdictionary);
    }
    _makeXMLHttpRequest(url, stringCallback, errCallback, useArrayBuffer, timeoutMillis);
}
function makeSynchronousXMLHttpRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    var useArrayBuffer = false;
    var errCallback = function(msg) {
        errorHandle(msg, userdictionary);
    }
    var responseText = _makeSyncXMLHttpRequest(url, errCallback, useArrayBuffer);
    readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
}


function makeXDomainRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var stringCallback = function(responseText) {
        if (jQuery.browser.msie && (parseFloat(jQuery.browser.version) > 7.99)) {
            readStringMessagesWithGPBDelimitersIE8(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
        } else {
            readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
        }
    }
    var errCallback = function(msg) {
        errorHandle(msg, userdictionary);
    }
    _makeXDomainRequest(url, stringCallback, errCallback, timeoutMillis);
}
function makeBinaryActiveXObjectRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var stringCallback = function(responseText) {
        readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    var errCallback = function(msg) {
        errorHandle(msg, userdictionary);
    }
    var useBinary = true;
    _makeActiveXObjectRequest(url, stringCallback, errCallback, timeoutMillis, useBinary);
}

function readTypedArrayMessagesWithGPBDelimiters(msgUint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    if (msgUint8Array == undefined) {
        errorHandle('msgUint8Array cannot be undefined');
        return;
    }
    var arr = new Array(msgUint8Array.byteLength);
    for (var j = 0; j < msgUint8Array.byteLength; j++) {
        var b = msgUint8Array[j];
        arr[j] = b;
    }
    readResponseMessagesContainingGPBDelimiters(arr, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
}

function readStringMessagesWithGPBDelimiters(str, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary) {
    if (str == undefined) {
        ecback('str cannot be undefined');
        return;
    }
    var arr = new Array(str.length);
    var j;
    var mask = 0xff;
    for (j = 0; j < str.length; j++) {
        var c = str.charCodeAt(j);
        var b = c & mask;
        arr[j] = b;
    }

    readResponseMessagesContainingGPBDelimiters(arr, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary);
}
function readStringMessagesWithGPBDelimitersIE8(str, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary) {
    if (str == undefined) {
        ecback('str cannot be undefined');
        return;
    }
    /* tried various encodings for serving and reading ie8 and found that iso8859-1 was the best for most characters,
     * then replacing a range of character code points that map to CE (central european) encoding when
     * relevant worked best.  using CE encodings in the first place (via iso8859-2 or windows-1250) did not work.
     * Use this before mask*/
    var arr = new Array(str.length);
    var j;
    var mask = 0xff;
    for (j = 0; j < str.length; j++) {

        var c = str.charCodeAt(j);

        var b = c & mask;

        arr[j] = b;
    }
/*window['writeToElement']['innerHTML'] = window['writeToElement']['innerHTML']
+ "<br/> binary str len=" + str.length + " arr length=" + arr.length;*/

    readResponseMessagesContainingGPBDelimitersIE8(arr, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary);
}

function readResponseMessagesContainingGPBDelimitersIE8(arr, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary) {

    if (arr == undefined) {
        ecback('arr cannot be undefined');
        return;
    }
    var totnum = arr.length;
    var offset = 0;
    while (offset < totnum) {
        try {

            var tmp = [];
            var nlen = arr.length-offset;
            for (var i = 0; i < nlen; i++) {
                tmp[i] = arr[offset + i];
            }
            /*var stream = new PROTO.ByteArrayStream( arr.slice(offset, arr.length) );*/
            var stream = new PROTO.ByteArrayStream(tmp);
            var msgLength = PROTO.int32.ParseFromStream(stream);
            var pos = stream['read_pos_'];

            offset = offset + pos;
/*
window['writeToElement']['innerHTML'] = window['writeToElement']['innerHTML']
+ "<br/>mLen=" + msgLength + " off=" + offset + " str.pos=" + pos;
*/

            if (msgLength > 0) {
                var msgArray = [];
                for (var ii = 0; ii < msgLength; ii++) {
                    msgArray[ii] = arr[offset + ii];
                }
                /*var msgArray = arr.slice(offset, offset + msgLength);*/
                offset = offset + msgLength;

                var decodedmsg = readMessage(createPROTOMessageHandle, msgArray);

                if (decodedmsg != undefined) {
                    pmcback(decodedmsg, userdictionary);
                }

            } else {
                break;
            }
        } catch (e) {
        }
    }
    cback(userdictionary);
}

function readResponseMessagesContainingGPBDelimiters(arr, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary) {

    if (arr == undefined) {
        ecback('arr cannot be undefined');
        return;
    }
    var totnum = arr.length;
    var offset = 0;
    while (offset < totnum) {
        try {

            /* slice is (start, end).  presumably, we need less than a few bytes to read the message length, but lets use max 100 */
            var i2 = ((offset + 100) < arr.length) ? (offset + 100) : arr.length;

            var stream = new PROTO.ByteArrayStream( arr.slice(offset, i2) );

            var msgLength = PROTO.int32.ParseFromStream(stream);

            var pos = stream['read_pos_'];
            offset += pos;

            if (msgLength > 0) {
                var msgArray = arr.slice(offset, offset + msgLength);
                offset = offset + msgLength;

                var decodedmsg = readMessage(createPROTOMessageHandle, msgArray);

                if (decodedmsg != undefined) {
                    pmcback(decodedmsg, userdictionary);
                }
            } else {
                break;
            }
        } catch (e) {
            ecback(e.message);
        }
    }
    cback(userdictionary);
}

function readMessage(createPROTOMessageHandle, msgArray) {

    var decodedmsg;
    try {
        decodedmsg = createPROTOMessageHandle();
        var msgStream = new PROTO.ByteArrayStream(msgArray);

/*window['writeToElement']['innerHTML'] = window['writeToElement']['innerHTML']
    + "<br/>    " + msgStream;*/

        decodedmsg.ParseFromStream(msgStream);

/*window['writeToElement']['innerHTML'] = window['writeToElement']['innerHTML']
    + "<br/>    " + decodedmsg;*/

    } catch(e) {

    }
    return decodedmsg;
}

function _makeXMLHttpRequest(url, successCallback, errorCallback, useArrayBuffer, timeoutMillis) {
    var timerId;
    var clearedInterval = false;

    if (timeoutMillis == undefined) {
        timeoutMillis = 65000;
    }
    var xhr = new XMLHttpRequest();

    if ("withCredentials" in xhr) {

        xhr.overrideMimeType('text/plain; charset=x-user-defined');
        var async = true;

        try {
            xhr['setRequestHeader']("Content-Type", 'text/plain;charset=x-user-defined');
            if (jQuery.browser.mozilla) {
                xhr.addEventListener("progress",
                    function updateProgress(evt) {
                        if (evt.lengthComputable) {
                            var percentComplete = evt.loaded / evt.total;
                        }
                    }
                , false);
            }
        } catch(e) {

        }

        xhr.open('GET', url, async);

        if (useArrayBuffer) {
            xhr.responseType = 'arraybuffer';
        }

        var receivedResponse = false;
        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                clearInterval(timerId);

                if (xhr.status == '200') {

                    var response;
                    if (useArrayBuffer) {
                        response = xhr['response'];
                    } else {
                        response = xhr.responseText;
                    }
                    if (!receivedResponse) {
                        receivedResponse = true;
                        successCallback(response);
                    }
                } else {
                    errorCallback( xhr['statusText']);
                }
            }
        };

        timerId = setTimeout(function() {
            if (!clearedInterval) {
                clearedInterval = true;
                clearInterval(timerId);
                xhr['abort']();
                errorCallback('request timed out');
            }
        }, timeoutMillis);

        xhr['send']();

    } else {
        throw new Error('could not construct XMLHttpRequest');
    }
}

/**
 *xdomain request calls can only be GET or POST.
 *Only text/plain is supported for the request's Content-Type header.
 *No authentication or cookies will be sent with the request.
 *Requests targeted to Intranet URLs may only be made from the Intranet Zone.
 *Requests must be targeted to the same scheme as the hosting page.
 *
 */
function _makeXDomainRequest(url, successCallback, errorCallback, timeoutMillis) {
    if (typeof XDomainRequest != "undefined") {
        var xhr = undefined;
        try {
            xhr = new XDomainRequest();

            /* ForIE
             * http://blogs.msdn.com/b/ieinternals/archive/2010/05/13/xdomainrequest-restrictions-limitations-and-workarounds.aspx
             * ... 3. No custom headers may be added to the request
             * ... 4. Only text/plain is supported for the request's Content-Type header
             */
            if (xhr.contentType) {
                if (jQuery.browser.msie) {
                    xhr.contentType = 'text/plain';
                } else {
                    xhr.contentType = 'text/plain; charset=x-user-defined';
                }
            }

            xhr.onerror = function() {
                var msg = xhr.responseText;
                errorCallback( msg );
            };

            var count=0;
            xhr.onprogress = function() { /* needed for xdomainrequest*/
                count++;
            };

            xhr.onload = function() {
                var responseText;
                try {
                    responseText = xhr.responseText;
                } catch (e) {
                    if (xhr.responseBody) {
                        responseText = xhr.responseBody;
                    } else {
                        responseText = xhr.response;
                    }
                }

                successCallback(responseText);
            };

            xhr.timeout = timeoutMillis;

            xhr.ontimeout = function () {
                xhr.abort();
                errorCallback( 'request timed out');
            }

            /* to prevent caching by front-end server */
            if (url.match(/\?/)) {
                url = url + "&t=" + (new Date()).getTime();
            } else {
                url = url + "?t=" + (new Date()).getTime();
            }

            xhr.open('GET', url);

            if (jQuery.browser.mozilla) {
                xhr.send(undefined);
            } else {
                xhr.send();
            }

        } catch(e) {
            xhr.abort();
            throw new Error(e.message);
        }
    } else {
        throw new Error('Could not construct an XDomainRequest.');
    }
}

function _makeActiveXObjectRequest(url, successCallback, errorCallback, timeoutMillis, useBinary) {
    var timerId;
    var clearedInterval = false;

    if (timeoutMillis == undefined) {
        timeoutMillis = 65000;
    }

    var xhr = undefined;

    try {
        var activexmodes=["Msxml2.XMLHTTP.6.0","Msxml2.XMLHTTP.4.0",
            "Msxml2.XMLHTTP.3.0", "Msxml2.XMLHTTP", "Microsoft.XMLHTTP"];
        if (window.ActiveXObject){
            for (var i=0; i < activexmodes.length; i++){
                try {
                    xhr = new ActiveXObject(activexmodes[i]);
                    break;
                } catch(e) {
                }
            }
        }
        if (xhr == undefined) {
            throw new Error('Could not construct an ActiveXObject.');
        }

        var async = true;
        xhr.open('GET', url, async); /* this needs to be before onreadystatchange*/

        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                clearInterval(timerId);
                if (xhr.status == '200') {
                    var response = xhr.responseText;

                    if (useBinary && (xhr.responseBody != undefined)) {

                        var convertResponseBodyToText = function (binary) {
                            var byteMapping = {};
                            for ( var i = 0; i < 256; i++ ) {
                                for ( var j = 0; j < 256; j++ ) {
                                    byteMapping[ String.fromCharCode( i + j * 256 ) ] = String.fromCharCode(i) + String.fromCharCode(j);
                                }
                            }
                            var rawBytes = IEBinaryToArray_ByteStr(binary);
                            var lastChr = IEBinaryToArray_ByteStr_Last(binary);
                            return rawBytes.replace(/[\s\S]/g,
                                function( match ) { return byteMapping[match]; }) + lastChr;
                        };

                        response  = convertResponseBodyToText(xhr.responseBody);
                    }
                    successCallback(response);

                } else {
                    errorCallback( xhr.statusText);
                }
            }
        };
/* if no-cache is failing in your system:
        if (url.match(/\?/)) {
            url = url + "&t=" + (new Date()).getTime();
        } else {
            url = url + "?t=" + (new Date()).getTime();
        }
*/
        timerId = setTimeout(function() {
            if (!clearedInterval) {
                clearedInterval = true;
                clearInterval(timerId);
                xhr.abort();
                errorCallback('request timed out');
            }
        }, timeoutMillis);

        xhr.send();

    } catch(e) {
        clearInterval(timerId);
        errorCallback(e.message)
    }
}

function _makeSyncXMLHttpRequest(url, errorCallback, useArrayBuffer) {
    /* for use in web workers */
    try {
        var xhr = new XMLHttpRequest();

        if ("withCredentials" in xhr) {

            xhr.overrideMimeType('text/plain; charset=x-user-defined');
            var async = false;

            xhr.open('GET', url, async);

            if (useArrayBuffer) {
                try {
                    xhr.responseType = 'arraybuffer';
                } catch(e) {
                    errorCallback(e.message);
                    return undefined;
                }
            }

            xhr.send();

            var responseText;
            try {
                responseText = xhr.responseText;
                if (responseText == undefined) {
                    responseText = xhr.response;
                }
            } catch(e) {
                responseText = xhr.response;
            }

            return responseText;

        } else {
            errorCallback('Could not construct an XMLHttpRequest.');
        }
        return undefined;
    } catch(e) {
        var msg = '';
        switch (e.code) {
            case XMLHttpRequest.NETWORK_ERR:
            case 101:
                msg = 'cannot connect to network';
                break;
            case XMLHttpRequest.SYNTAX_ERR:
            case XMLHttpRequest.NOT_SUPPORTED_ERR:
                msg = 'error in client request';
                break;
            case XMLHttpRequest.SECURITY_ERR:
                msg = "request method isn't valid";
                break;
            default:
                msg = 'Unknown Error';
                break;
        }
        errorCallback(msg);
        return undefined;
    }
}

function _makeSyncActiveXObjectRequest(url, errorCallback, useBinary) {
    /* for use in web workers */
    try {
        var xhr = undefined;
        var activexmodes=["Msxml2.XMLHTTP", "Microsoft.XMLHTTP",
            "Msxml2.XMLHTTP.6.0","Msxml2.XMLHTTP.3.0","Msxml2.XMLHTTP"];
        if (window.ActiveXObject){
            for (var i=0; i < activexmodes.length; i++){
                try {
                    xhr = new ActiveXObject(activexmodes[i]);
                    break;
                } catch(e){
                }
            }
        }
        if (xhr == undefined) {
            errorCallback('Could not construct an ActiveXObject.');
            return undefined;
        }

        var async = false;

        if (url.match(/\?/)) {
            url = url + "&t=" + (new Date()).getTime();
        } else {
            url = url + "?t=" + (new Date()).getTime();
        }
        xhr.open('GET', url, async);

        xhr.send();

        if (xhr.status == '200') {

            if (useBinary && xhr.responseBody) {

                var convertResponseBodyToText = function (binary) {
                    var byteMapping = {};
                    for ( var i = 0; i < 256; i++ ) {
                        for ( var j = 0; j < 256; j++ ) {
                            byteMapping[ String.fromCharCode( i + j * 256 ) ] = String.fromCharCode(i) + String.fromCharCode(j);
                        }
                    }
                    var rawBytes = IEBinaryToArray_ByteStr(binary);
                    var lastChr = IEBinaryToArray_ByteStr_Last(binary);
                    return rawBytes.replace(/[\s\S]/g,
                        function( match ) { return byteMapping[match]; }) + lastChr;
                };

                return convertResponseBodyToText(xhr.responseBody);
            } else {
                return xhr.responseText;
            }
        }
        return xhr.statusText;

    } catch(e) {
        var msg = '';
        switch (e.code) {
            case XMLHttpRequest.NETWORK_ERR:
            case 101:
                msg = 'cannot connect to network';
                break;
            case XMLHttpRequest.SYNTAX_ERR:
            case XMLHttpRequest.NOT_SUPPORTED_ERR:
                msg = 'error in client request';
                break;
            case XMLHttpRequest.SECURITY_ERR:
                msg = "request method isn't valid";
                break;
            default:
                msg = 'Unknown Error';
                break;
        }
        errorCallback(msg);
    }

}
