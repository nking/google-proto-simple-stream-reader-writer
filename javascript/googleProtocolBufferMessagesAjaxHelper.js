/*
* Methods to help w/ browser specific ajax calls and parsing of responses.
*
* A few things found:
*
*    -- For IE6-IE8 this combination works:
*       server settings:
*            content-type=octet/stream
*            character encoding=UTF-8
*       google protocol buffers written to stream using the message's .writeDelimitedTo(out)
*       and client AJAX being ActiveXObject with attempts for "Msxml2.XMLHTTP.6.0","Msxml2.XMLHTTP.4.0",
*           "Msxml2.XMLHTTP.3.0", "Msxml2.XMLHTTP", "Microsoft.XMLHTTP"
*
*    -- For IE9, this combination works:
*       server settings:
*            content-type=octet/stream
*            character encoding=UTF-7 <==
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

function makeXMLHttpRequestForArrayBufferWithTypedArray(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var useArrayBuffer = true;
    var typedArrayCallback = function(response) {
        readTypedArrayMessagesWithGPBDelimiters(response, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeXMLHttpRequest(url, typedArrayCallback, errorHandle, useArrayBuffer, timeoutMillis);
}
function makeXMLHttpRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var useArrayBuffer = false;
    var stringCallback = function(responseText) {
        readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeXMLHttpRequest(url, stringCallback, errorHandle, useArrayBuffer, timeoutMillis);
}
function makeSynchronousXMLHttpRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    var useArrayBuffer = false;
    var responseText = _makeSyncXMLHttpRequest(url, errorHandle, useArrayBuffer);
    readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
}


function makeXDomainRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var stringCallback = function(responseText) {
        readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeXDomainRequest(url, stringCallback, errorHandle, timeoutMillis);
}
function makeBinaryActiveXObjectRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var stringCallback = function(responseText) {
        readStringMessagesWithGPBDelimiters(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    var useBinary = true;
    _makeActiveXObjectRequest(url, stringCallback, errorHandle, timeoutMillis, useBinary);
}


function readTypedArrayMessagesWithGPBDelimiters(msgUint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    if (msgUint8Array == undefined) {
        errorHandle('msgUint8Array cannot be undefined');
        return;
    }
    var array = new Array(msgUint8Array.byteLength);
    var j;
    for (j = 0; j < msgUint8Array.byteLength; j++) {
        array[j] = msgUint8Array[j];
    }
    readResponseMessagesContainingGPBDelimiters(array, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
}

function readStringMessagesWithGPBDelimiters(str, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary) {
    if (str == undefined) {
        ecback('str cannot be undefined');
        return;
    }
    var array = new Array(str.length);
    var j;
    for (j = 0; j < str.length; j++) {
        var c = str.charCodeAt(j);
        var b = c & 0xff;
        array[j] = b;
    }
    readResponseMessagesContainingGPBDelimiters(array, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary);
}

function readResponseMessagesContainingGPBDelimiters(array, createPROTOMessageHandle, pmcback, cback, ecback, userdictionary) {
    if (array == undefined) {
        ecback('array cannot be undefined');
        return;
    }
    var totnum = array.length;
    var offset = 0;

    while (offset < totnum) {
        try {
            var stream = new PROTO.ByteArrayStream( array.slice(offset, array.length) );
            var msgLength = PROTO.int32.ParseFromStream(stream);
            offset = offset + stream.read_pos_;

            var msgArray = array.slice(offset, offset + msgLength);
            offset = offset + msgLength;

            var decodedmsg = readMessage(createPROTOMessageHandle, msgArray);

            if (decodedmsg != undefined) {
                pmcback(decodedmsg, userdictionary);
            }
        } catch (e) {

        }
    }
    cback(userdictionary);
}

function readMessage(createPROTOMessageHandle, msgArray) {
    var decodedmsg;
    try {
        decodedmsg = createPROTOMessageHandle();
        var msgStream = new PROTO.ByteArrayStream(msgArray);
        decodedmsg.ParseFromStream(msgStream);
    } catch(e) {

    }
    return decodedmsg;
}

function _makeXMLHttpRequest(url, successCallback, errorCallback, useArrayBuffer, timeoutMillis) {
    var timerId;
    var clearedInterval = false;

    if (timeoutMillis == undefined) {
        timeoutMillis = 15000;
    }

    var xhr = new XMLHttpRequest();

    if ("withCredentials" in xhr) {

        xhr.overrideMimeType('text/plain; charset=x-user-defined');
        var async = true;

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
                        response = xhr.response;
                    } else {
                        response = xhr.responseText;
                    }
                    if (!receivedResponse) {
                        receivedResponse = true;
                        successCallback(response);
                    }
                } else {
                    errorCallback( xhr.statusText);
                }
            }
        };
        timerId = setTimeout(function() {
            if (!clearedInterval) {
                clearedInterval = true;
                clearInterval(timerId);
                xhr.abort();
                errorCallback('request timed out');
            }
        }, timeoutMillis);
        xhr.send();
    } else {
        throw new Error('could not construct XMLHttpRequest');
    }
}

function _makeXDomainRequest(url, successCallback, errorCallback, timeoutMillis) {

    if (typeof XDomainRequest != "undefined") {
        var xhr;
        try {
            xhr = new XDomainRequest();

            xhr.contentType = 'text/plain; charset=x-user-defined';

            xhr.onerror = function(e){
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

            xhr.send();

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
        timeoutMillis = 15000;
    }

    var xhr;

    try {
        var activexmodes=["Msxml2.XMLHTTP.6.0","Msxml2.XMLHTTP.4.0",
            "Msxml2.XMLHTTP.3.0", "Msxml2.XMLHTTP", "Microsoft.XMLHTTP"];
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
        var xhr;
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