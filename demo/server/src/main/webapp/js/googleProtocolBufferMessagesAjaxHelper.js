/*
* Methods to help w/ browser specific ajax calls and parsing of responses.
*
* A few things found:
*    -- Server response should use character encoding ISO-8859-1 and content-type text/plain
*       due to IE limitations.
*    -- Google Protocol Buffers (without additional delimiters) are successfully
*       passed to the ajax objects as responseText or response for all browers if
*       server settings above are used.
*    -- Google Protocol Buffers with the delimiters are successfully passed to the ajax
*       objects as responseText or response for webkit and mozilla based browsers, but
*       not for IE, excepting XDomainRequest capable browsers.
*       For IE (ActiveXObject ajax objects), the data is present in the ajax object's responseBody
*       and is only accessible to IE specific languages or components such as vbscript
*       or jscript.  The additional processing time to access the data and the fact
*       that one can't use web workers yet in IE means that one should
*       instead use Google Protocol Buffers without delimiters for IE.
*
*
* The generated messages are Google Protocol Buffer messages, whose templates
* were compiled from the Google Protocol Buffer library
* http://code.google.com/apis/protocolbuffers/
*
* The messages are sent over the network separated by a byte marker delimiter.
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
 *
 */

function makeXMLHttpRequestForArrayBufferWithTypedArrayNoDelimiters(url, successCallback, errorCallback, createPROTOMessages, timeoutMillis) {
    var useArrayBuffer = true;
    var typedArrayCallback = function(response) {
        typedArrayHandlerNoDelimiters(response, successCallback, errorCallback, createPROTOMessages);
    }
    _makeXMLHttpRequest(url, typedArrayCallback, errorCallback, useArrayBuffer, timeoutMillis);
}
function makeXMLHttpRequestForArrayBufferWithTypedArray(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var useArrayBuffer = true;
    var typedArrayCallback = function(response) {
        typedArrayHandler(response, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeXMLHttpRequest(url, typedArrayCallback, errorHandle, useArrayBuffer, timeoutMillis);
}

function makeXMLHttpRequestWithoutArrayBufferNoDelimiters(url, successCallback, errorCallback, createPROTOMessages, timeoutMillis) {
    var useArrayBuffer = false;
    var stringCallback = function(responseText) {
        readResponseStringMessagesNoDelimiters(responseText, successCallback, errorCallback, createPROTOMessages);
    }
    _makeXMLHttpRequest(url, stringCallback, errorCallback, useArrayBuffer, timeoutMillis);
}
function makeXMLHttpRequestWithoutArrayBuffer(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var useArrayBuffer = false;
    var stringCallback = function(responseText) {
        readMessagesFromBinaryString(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeXMLHttpRequest(url, stringCallback, errorHandle, useArrayBuffer, timeoutMillis);
}

function makeXDomainRequestNoDelimiters(url, successCallback, errorCallback, createPROTOMessages) {
    var stringCallback = function(responseText) {
        readResponseStringMessagesNoDelimiters(responseText, successCallback, errorCallback, createPROTOMessages);
    }
    _makeXDomainRequest(url, stringCallback, errorCallback);
}
function makeXDomainRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    var stringCallback = function(responseText) {
        readMessagesFromBinaryString(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeXDomainRequest(url, stringCallback, errorHandle);
}

function makeActiveXObjectRequestNoDelimiters(url, successCallback, errorCallback, createPROTOMessages, timeoutMillis) {
    var stringCallback = function(responseText) {
        readResponseStringMessagesNoDelimiters(responseText, successCallback, errorCallback, createPROTOMessages);
    }
    _makeActiveXObjectRequest(url, stringCallback, errorCallback, timeoutMillis);
}
function makeActiveXObjectRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary, timeoutMillis) {
    var stringCallback = function(responseText) {
        readMessagesFromBinaryString(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    }
    _makeActiveXObjectRequest(url, stringCallback, errorHandle, timeoutMillis);
}

function makeSyncXMLHttpRequestNoDelimiters(url, successCallback, errorCallback, createPROTOMessages) {
    var useArrayBuffer = false;
    var responseText = _makeSyncXMLHttpRequest(url, errorCallback, useArrayBuffer);
    readResponseStringMessagesNoDelimiters(responseText, successCallback, errorCallback, createPROTOMessages);
}
function makeSyncXMLHttpRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    var useArrayBuffer = false;
    var responseText = _makeSyncXMLHttpRequest(url, errorHandle, useArrayBuffer);
    readMessagesFromBinaryString(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
}

function makeSyncActiveXObjectRequestNoDelimiters(url, successCallback, errorCallback, createPROTOMessages) {
    var useArrayBuffer = false;
    var responseText = _makeSyncActiveXObjectRequest(url, errorCallback, useArrayBuffer, createPROTOMessages);
    readResponseStringMessagesNoDelimiters(responseText, successCallback, errorCallback, createPROTOMessages);
}
function makeSyncActiveXObjectRequest(url, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    var useArrayBuffer = false;
    var responseText = _makeSyncActiveXObjectRequest(url, errorHandle, useArrayBuffer, createPROTOMessage);
    readMessagesFromBinaryString(responseText, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
}

/***   functions below here are support for the public messages above  ***/
function typedArrayHandlerNoDelimiters(response, successCallback, errorCallback, createPROTOMessages) {
    var msgUint8Array;
    try {
        msgUint8Array = new Uint8Array(response);
        readTypedArrayMessagesNoDelimiters(msgUint8Array, successCallback, errorCallback, createPROTOMessages);
    } catch(e) {
        errorCallback(e.message);
    }
}
function typedArrayHandler(response, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary) {
    var msgUint8Array;
    try {
        msgUint8Array = new Uint8Array(response);
        readMessagesFromUint8Array(msgUint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorHandle, userdictionary);
    } catch(e) {
        errorHandle(e.message);
    }
}
function readTypedArrayMessagesNoDelimiters(msgUint8Array, cb, errCB, createPROTOMessageHandle) {
    if (msgUint8Array == undefined) {
        errCB('msgUint8Array cannot be undefined');
        return;
    }
    var array = new Array(msgUint8Array.byteLength);
    for (var j = 0; j < msgUint8Array.byteLength; j++) {
        array[j] = msgUint8Array[j];
    }
    try {
        var decodedmsgs = createPROTOMessageHandle();
        var stream = new PROTO.ByteArrayStream(array);
        decodedmsgs.ParseFromStream(stream);
        cb(decodedmsgs.msg);
    } catch(e) {
        errCB(e.message);
    }
}

function readResponseStringMessagesNoDelimiters(str, cback, ecback, createPROTOMessageHandle) {
    if (str == undefined) {
        ecback('str cannot be undefined');
        return;
    }
    var array = new Array(str.length);
    for (var j = 0; j < str.length; j++) {
        var c = str.charCodeAt(j);
        var b = c & 0xff;
        array[j] = b;
    }
    try {
        var decodedmsgs = createPROTOMessageHandle();
        var stream = new PROTO.ByteArrayStream(array);
        decodedmsgs.ParseFromStream(stream);
        cback(decodedmsgs.msg);
    } catch(e) {
        ecback(e.message);
    }
}

function _makeXMLHttpRequest(url, successCallback, errorCallback, useArrayBuffer, timeoutMillis) {
    var timerId;
    var clearedInterval = false;

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
            var x = xhr;
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
        errorCallback('Could not construct an XMLHttpRequest.');
    }
}

function _makeXDomainRequest(url, successCallback, errorCallback) {
    var xhr;

    if (typeof XDomainRequest != "undefined") {

        xhr = new XDomainRequest();

        xhr.onerror = function(e){
            var msg = xhr.responseText;
            errorCallback( msg );
        };

        xhr.onload = function() {
            var responseText;
            try {
                responseText = xhr.data.responseText;
            } catch (e) {
                if (xhr.responseBody) {
                    responseText = xhr.responseBody;
                } else {
                    responseText = xhr.responseText;
                }
            }
            successCallback(responseText);
        };

        xhr.open('GET', url);
        xhr.contentType = 'text/plain; charset=x-user-defined';
        xhr.timeout = function () {
            xhr.abort();
            errorCallback( 'request timed out');
        }

        xhr.send(null);

    } else {
        errorCallback('Could not construct an XDomainRequest.');
    }
}

function _makeActiveXObjectRequest(url, successCallback, errorCallback, timeoutMillis) {
    var timerId;
    var clearedInterval = false;

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
        return;
    }

    xhr.onreadystatechange = function() {
        var x = xhr;
        if (x.readyState == 4) {
            clearInterval(timerId);
            if (x.status == '200') {

                var response = x.responseText;
                /*
                if ((response == undefined) || (response.length == 0) && (x.responseBody != undefined)) {
                    response = x.responseBody;
                    /* This is an IE native array of bytes not accessible by javascript, but accessible via
                     * vbscript or jscript.  Using vbscript was cpu intense so didn't include it here, but here's a very helpful
                     * page on now to do that:
                     * http://miskun.com/javascript/internet-explorer-and-binary-files-data-access/
                     *
                     * Instead of using this method, will choose to always stream messages without delimiters
                     * for IE requests.
                }*/
                successCallback(response);

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

    var async = true;

    try{
        xhr.setRequestHeader('text/plain; charset=x-user-defined');
    } catch(e) {}

    xhr.open('GET', url, async);

    xhr.send(null);
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

function _makeSyncActiveXObjectRequest(url, errorCallback) {
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
        xhr.open('GET', url, async);

        xhr.send(null);

        if (xhr.status == '200') {
            return xhr.responseText;
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