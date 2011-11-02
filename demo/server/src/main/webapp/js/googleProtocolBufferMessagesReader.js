/* 
* library to parse google protocol buffer messages and pass them back as
* each is deserialized.
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
* 
*/

/**
* For browsers which do not support FileSystem API nor BlobBuilder, but do support binary and
* Typed Arrays, this method accepts a signed Uint8Array filled from the xmlhttprequest binary 'response'.
* A function handle to create the PROTO message needs to be provided and a 
* The deserialized messages are returned to perMessageCallback as each are parsed out of the
* binary response.
* 
* Function arguments:
* 
* @param uint8Array is a Uint8Array of binary streamed google protocol buffer messages
* 
* @param createPROTOMessage is the handle to a function which creates an instance of 
* the gpb generated message which is in your .proto.js created w/ sirikata library. 
*    For Example, in calling code:
*         this.createPROTOMessage = function createPROTOMessage() {
*            return new climbwithyourfeet.EventPB;
*        }
*       
* @param perMessageCallback is a function that will be used as each message is deserialized.
* the perMessageCallback function should accept arguments 
* an instance of type protoMessageType and an associative array useful for handling the result (can be null).
*    For Example, in calling code:
*        this.perMessageCallback = function renderEvent(gpb, dictionary) {
*           if (dictionary['categorySelection'] == currentCategory) {
*               document.getElementById("output").innerHTML = gpb.toString();
*           }
*        }
* 
* @param completedCallback is a function that will be used after all messages are deserialized.
* the completedCallback function should accept an argument of an associative array which is
* useful for handling the result (can be null). 
*    For Example, in calling code:
*        this.completedCallback = function deserializationCompleted(dictionary) {
*           if (dictionary['categorySelection'] == currentCategory) {
*               renderRemainingEvents(dictionary['categorySelection']);
*           }
*        }
* 
* @param errorCallback is the handle to a function that should accept arguments
* of a string error message and an associative array of information useful for handling the result (can be null).
*    For Example, in calling code:
*        this.errorCallback = function (errorMessage, dictionary) {
*            document.getElementById("output").innerHTML = errorMessage;
*        }
*        
* @param dictionary is an associative array that one can use to pass back pararameters
*    in the callbacks.
*    For Example, in calling code:
*        var dictionary = {'categorySelection' : category};
*/
function readMessagesFromUint8Array(uint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary) {
    if (uint8Array == undefined) {
        errorCallback('uint8Array cannot be null', dictionary);
    }
    _readMessagesFromUint8ArrayIteratively(0, uint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary);
}

/**
* For browsers which do not support FileSystem API nor BlobBuilder nor Typed Arrays,
* this method accepts the binary string responseText from an xmlhttprequest.
* The deserialized messages are returned to perMessageCallback as each are parsed out of the
* binary response.
* Function arguments:
* 
* @param binaryString is the binary response text of streamed google protocol buffer messages
* 
* @param createPROTOMessage is the handle to a function which creates an instance of 
* the gpb generated message. 
*    For Example, in calling code:
*         this.createPROTOMessage = function createPROTOMessage() {
*            return new climbwithyourfeet.EventPB;
*        }
* 
* @param perMessageCallback is a function that will be used as each message is deserialized.
* the perMessageCallback function should accept arguments 
* an instance of type protoMessageType and an associative array useful for handling the result (can be null).
*    For Example, in calling code:
*        this.perMessageCallback = function renderEvent(gpb, dictionary) {
*           if (dictionary['categorySelection'] == currentCategory) {
*               document.getElementById("output").innerHTML = gpb.toString();
*           }
*        }
* 
* @param completedCallback is a function that will be used after all messages are deserialized.
* the completedCallback function should accept an argument of an associative array which is
* useful for handling the result (can be null). 
*    For Example, in calling code:
*        this.completedCallback = function deserializationCompleted(dictionary) {
*           if (dictionary['categorySelection'] == currentCategory) {
*               renderRemainingEvents(dictionary['categorySelection']);
*           }
*        }
* 
* @param errorCallback is the handle to a function that should accept arguments
* of a string error message and an associative array of information useful for handling the result (can be null).
*    For Example, in calling code:
*        this.errorCallback = function (errorMessage, dictionary) {
*            document.getElementById("output").innerHTML = errorMessage;
*        }
*        
* @param dictionary is an associative array that one can use to pass back pararameters
*    in the callbacks.
*    For Example, in calling code:
*        var dictionary = {'categorySelection' : category};
*
*/
function readMessagesFromBinaryString(binaryString, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary) {
    _readMessagesFromBinaryStringIteratively(0, binaryString, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary);
}

/*
 *==============================================================================================
 *  functions below here are support for the public functions above and should 
 *  not normally be used by caller
 *==============================================================================================
*/


function _readMessagesFromUint8ArrayIteratively(startOffset, uint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary) {    
    if (uint8Array == undefined) {
        errorCallback('_readMessagesFromUint8ArrayIteratively: unint8Array cannot be null', dictionary);
        return;
    } else if (startOffset >= (uint8Array.byteLength - 1)) {
        completedCallback(dictionary);
        return;
    }
    var byteMarkerSize = 5;

    var msgLength = _readByteMarkerIntoInt32(uint8Array, startOffset, startOffset + byteMarkerSize);
    
    if (msgLength) {
        
        var decodedmsg = createPROTOMessage();
        
        startOffset += byteMarkerSize;
        var stopOffset = startOffset + msgLength;
        
        _readMessageFromUint8Array(uint8Array, startOffset, stopOffset, decodedmsg);
            
        perMessageCallback(decodedmsg, dictionary);
        
        startOffset+= msgLength;
        
        _readMessagesFromUint8ArrayIteratively(startOffset, uint8Array, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary);
    }
}
function _readByteMarkerIntoInt32(markerUint8Array, startOffset, stopOffset) {
    if (markerUint8Array[startOffset] != 0) {
        return undefined;
    }
    return _readByteMarker(markerUint8Array, startOffset + 1, stopOffset, 0, 0);
}
function _readByteMarker(markerUint8Array, startOffset, stopOffset, total, index) {
    if (startOffset >= stopOffset) {
        return total;
    }
    var b = markerUint8Array[startOffset];
    total += (b & 0x7f) << (index*7);
    startOffset++;
    index++;
    return _readByteMarker(markerUint8Array, startOffset, stopOffset, total, index);
}
function _readMessageFromUint8Array(msgUint8Array, startOffset, stopOffset, decodedMessage) {
    var array = new Array(stopOffset - startOffset);
    var i = 0;
    for (var j = startOffset; j < stopOffset; j++) {
        array[i] = msgUint8Array[j];
        i++;
    }
    var stream = new PROTO.ByteArrayStream(array);
    decodedMessage.ParseFromStream(stream);
}

function _readMessagesFromBinaryStringIteratively(startOffset, binaryString, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary) {
    if (binaryString == undefined) {
        errorCallback('_readMessagesFromBinaryStringIteratively: binaryString cannot be null', dictionary);
        return;
    } else if (startOffset >= (binaryString.length - 1)) {
        completedCallback(dictionary);
        return;
    }

    var byteMarkerSize = 5;
    var msgLength = _readByteMarkerStringIntoInt32(binaryString, startOffset, startOffset + byteMarkerSize);
    
    if (msgLength) {
        var decodedmsg = createPROTOMessage();
        
        startOffset += byteMarkerSize;
        var stopOffset = startOffset + msgLength;
        
        _readMessageFromBinaryString(binaryString, startOffset, stopOffset, decodedmsg);
            
        perMessageCallback(decodedmsg, dictionary);
        
        startOffset+= msgLength;
        
        _readMessagesFromBinaryStringIteratively(startOffset, binaryString, createPROTOMessage, perMessageCallback, completedCallback, errorCallback, dictionary);
    }
}
function _readByteMarkerStringIntoInt32(binaryString, startOffset, stopOffset) {
    var startByte = binaryString.charCodeAt(startOffset);
    startByte = startByte & 0x7f;
    if (startByte != 0){
        return undefined;
    }
    return _readByteMarkerString(binaryString, startOffset + 1, stopOffset, 0, 0);
}
function _readByteMarkerString(binaryString, startOffset, stopOffset, total, index) {
    if (startOffset >= stopOffset) {
        return total;
    }
    var b = binaryString.charCodeAt(startOffset);
    total += (b & 0x7f) << (index*7);
    startOffset++;
    index++;
    return _readByteMarkerString(binaryString, startOffset, stopOffset, total, index);
}
function _readMessageFromBinaryString(binaryString, startOffset, stopOffset, decodedMessage) {
    var array = new Array(stopOffset - startOffset);
    var i = 0;
    for (var j = startOffset; j < stopOffset; j++) {
        var c = binaryString.charCodeAt(j);
        var b = c & 0xff;
        array[i] = b;
        i++;
    }
    var stream = new PROTO.ByteArrayStream(array);
    decodedMessage.ParseFromStream(stream);
}
