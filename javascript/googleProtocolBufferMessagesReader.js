/* 
* Client to parse google protocol buffer messages.
* The generated messages are Google Protocol Buffer messages, whose templates
* were compiled from the Google Protocol Buffer library
* http://code.google.com/apis/protocolbuffers/
* 
* The messages are sent over the network separated by a byte marker delimiter.
* The library that writes the delimiters and the messages to the network stream is
*    http://code.google.com/p/google-proto-simple-stream-reader-writer/
* 
* This client reads the binary message delimeters 
* and then uses
* protobuf.js https://github.com/sirikata/protojs/blob/master/protobuf.js
* and pbj.js https://github.com/sirikata/protojs/blob/master/pbj.js
* to deserialize the messages.
* 
* The readMarkerAndMessages method below expects a binary file or blob of the downloaded messages.
* 
*/

self.requestFileSystemSync =  self.webkitRequestFileSystemSync || self.requestFileSystemSync;
self.BlobBuilder = self.WebKitBlobBuilder || self.MozBlobBuilder || self.BlobBuilder;
if ((typeof File !== 'undefined') && !File.prototype.slice) {
    if (File.prototype.webkitSlice) {
        File.prototype.slice = File.prototype.webkitSlice;
    } else if (File.prototype.mozSlice) {
        File.prototype.slice = File.prototype.mozSlice;
    }
}
if ((typeof Blob !== 'undefined') && !Blob.prototype.slice) {
    if (Blob.prototype.webkitSlice) {
        Blob.prototype.slice = Blob.prototype.webkitSlice;
    } else if (File.prototype.mozSlice) {
        Blob.prototype.slice = Blob.prototype.mozSlice;
    }
}

/** 
* Read all gpb messages from a file or blob, passing each back to the caller in perMessageCallback.
* 
* Function arguments:
* 
* @param fileOrBlob is file or blob of binary streamed google protocol buffer messages
* 
* @param createPROTOMessage is the handle to a function which creates an instance of 
* the gpb generated message. 
*    For Example, in calling code:
*         this.createPROTOMessage = function createPROTOMessage() {
*            return new climbwithyourfeet.EventPB;
*        }
* 
* @param perMessageCallback is a function that will be used as each message is
* parsed.  the perMessageCallback function should accept an argument
* that is an instance of  type protoMessageType.
*    For Example, in calling code:
*        this.perMessageCallback = function renderEvent(eventPB) {
*           var oEl = document.getElementById("output");
*           oEl.innerHTML = oEl.innerHTML + eventPB.toString();
*        }
*/
function readMarkerAndMessages(fileOrBlob, createPROTOMessage, perMessageCallback) {
    readMarkerAndMessagesIteratively(fileOrBlob, 0, createPROTOMessage, perMessageCallback);
}

/**
* For browsers which do not implement FileSystem API nor BlobBuilder, but do implement Blob.
* The given array is filled from the request binary response data written into an Int8Array.
* 
* Function arguments:
* 
* @param int8Array is an Int8Array of binary streamed google protocol buffer messages
* 
* @param createPROTOMessage is the handle to a function which creates an instance of 
* the gpb generated message. 
*    For Example, in calling code:
*         this.createPROTOMessage = function createPROTOMessage() {
*            return new climbwithyourfeet.EventPB;
*        }
*       
* @param perMessageCallback is a function that will be used as each message is
* parsed.  the perMessageCallback function should accept an argument
* that is an instance of  type protoMessageType.
*    For Example, in calling code:
*        this.perMessageCallback = function renderEvent(eventPB) {
*           var oEl = document.getElementById("output");
*           oEl.innerHTML = oEl.innerHTML + eventPB.toString();
*        }
*/
function readMessagesFromInt8Array(int8Array, createPROTOMessage, perMessageCallback) {
    readMessagesFromInt8ArrayIteratively(0, int8Array, createPROTOMessage, perMessageCallback);
}

/**
* For browsers which do not implement FileSystem API nor BlobBuilder, nor Blob, but does
* implement XMLHttpRequest. This binary string array is the binary response text.
* 
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
* @param perMessageCallback is a function that will be used as each message is
* parsed.  the perMessageCallback function should accept an argument
* that is an instance of  type protoMessageType.
*    For Example, in calling code:
*        this.perMessageCallback = function renderEvent(eventPB) {
*           var oEl = document.getElementById("output");
*           oEl.innerHTML = oEl.innerHTML + eventPB.toString();
*        }
 */
function readMessagesFromBinaryString(binaryString, createPROTOMessage, perMessageCallback) {
    readMessagesFromBinaryStringIteratively(0, binaryString, createPROTOMessage, perMessageCallback);
}


/*
 *==============================================================================================
 *  functions below here are support for the public functions above and should 
 *  not normally be used by caller
 *==============================================================================================
*/


/* Most users should use  readMarkerAndMessages(fileOrBlob, createPROTOMessage, perMessageCallback)
 * instead of this method
 */
function readMarkerAndMessagesIteratively(fileOrBlob, startOffset, createPROTOMessage, perMessageCallback) {

    if (startOffset >= fileOrBlob.size) {
        return;
    }

    var byteMarkerSize = 5;
    
    var byteMarkerReader = new FileReader();

    byteMarkerReader.onload = function(e) {
        var arrayBuffer = e.target.result;
        readMessages(fileOrBlob, arrayBuffer, startOffset, byteMarkerSize, createPROTOMessage, perMessageCallback);
    }
    byteMarkerReader.onerror = function(e) {
        //console.log('    error while reading event byte marker from fileOrBlob:', e);
        return;
    }
    byteMarkerReader.onabort = function(e) {
        //console.log('    error while reading event byte marker from fileOrBlob:', e);
        return;
    }
    var byteMarkerBlob = fileOrBlob.slice(startOffset, startOffset + byteMarkerSize);

    byteMarkerReader.readAsArrayBuffer(byteMarkerBlob);
}
/* shouldn't be invoked outside of this script */
function readMessages(fileOrBlob, arrayBuffer, startOffset, byteMarkerSize, createPROTOMessage, perMessageCallback) {
    
    var byteMarkerInt8Array = new Int8Array(arrayBuffer, 0, byteMarkerSize);
    
    var msgLength = readByteMarkerIntoInt32(byteMarkerInt8Array, 0, byteMarkerSize);
    /*console.log('    eventLength:', eventLength);*/
                
    if (msgLength) {
        var eventBytesReader = new FileReader();
        var offset = startOffset + byteMarkerSize;
        var msgBlob = fileOrBlob.slice(offset, offset + msgLength);

        eventBytesReader.onload = function(e) {
            var arrayBuffer = e.target.result;
             
            var decodedmsg = createPROTOMessage();
            
            var msgArray = new Int8Array(arrayBuffer, 0, arrayBuffer.byteLength);
            readMessageFromInt8Array(msgArray, 0, msgLength, decodedmsg);
            
            perMessageCallback(decodedmsg);
                                                
            /* start a new read */
            readMarkerAndMessagesIteratively(fileOrBlob, offset + msgLength, createPROTOMessage, perMessageCallback);
        }
        eventBytesReader.onerror = function(e) {
            //console.log('    error while reading event bytes from fileOrBlob: ', e);
            return;
        }

        eventBytesReader.readAsArrayBuffer(msgBlob);
    }
}

/* shouldn't be invoked outside of this script */
function readMessageFromInt8Array(msgInt8Array, startOffset, stopOffset, decodedMessage) {
    /* for now, need to convert to array */
    var array = new Array(stopOffset - startOffset);
    var i = 0;
    for (var j = startOffset; j < stopOffset; j++) {
        array[i] = msgInt8Array[j];
        i++;
    }
    var stream = new PROTO.ByteArrayStream(array);
    decodedMessage.ParseFromStream(stream);
}

/* shouldn't be invoked outside of this script */
function readByteMarkerIntoInt32(markerInt8Array, startOffset, stopOffset) {
    /* first is 0x00*/
    if (markerInt8Array[startOffset] != 0) {
        return undefined;
    }
    return readByteMarker(markerInt8Array, startOffset + 1, stopOffset, 0, 0);
}
function readByteMarker(markerInt8Array, startOffset, stopOffset, total, index) {
    if (startOffset >= stopOffset) {
        return total;
    }
    //byte markers are signed, hold values 0-127
    var b = markerInt8Array[startOffset];
    total += (b & 0x7f) << (index*7);
    startOffset++;
    index++;
    return readByteMarker(markerInt8Array, startOffset, stopOffset, total, index);
}


function readMessagesFromInt8ArrayIteratively(startOffset, int8Array, createPROTOMessage, perMessageCallback) {
    
    //console.log("readEventsFromIntArraysIteratively");
    
    if (startOffset >= int8Array.byteLength) {
        return;
    }

    var byteMarkerSize = 5;

    // read startOffset, startOffset + byteMarkerSize from byteMarkerInt8Array
    var msgLength = readByteMarkerIntoInt32(int8Array, startOffset, startOffset + byteMarkerSize);
    
    if (msgLength) {
        
        var decodedmsg = createPROTOMessage();
        
        startOffset += byteMarkerSize;
        var stopOffset = startOffset + msgLength;
        
        readMessageFromInt8Array(int8Array, startOffset, stopOffset, decodedmsg);
            
        perMessageCallback(decodedmsg);
        
        startOffset+= msgLength;
        
        readMessagesFromInt8ArrayIteratively(startOffset, int8Array, createPROTOMessage, perMessageCallback)
    }
}

function readMessagesFromBinaryStringIteratively(startOffset, binaryString, createPROTOMessage, perMessageCallback) {
    
    //console.log("readMessagesFromBinaryStringIteratively");
    
    if (startOffset >= binaryString.length) {
        return;
    }

    var byteMarkerSize = 5;

    // read startOffset, startOffset + byteMarkerSize from byteMarkerInt8Array
    var msgLength = readByteMarkerStringIntoInt32(binaryString, startOffset, startOffset + byteMarkerSize);
    
    if (msgLength) {
        
        var decodedmsg = createPROTOMessage();
        
        startOffset += byteMarkerSize;
        var stopOffset = startOffset + msgLength;
        
        readMessageFromBinaryString(binaryString, startOffset, stopOffset, decodedmsg);
            
        perMessageCallback(decodedmsg);
        
        startOffset+= msgLength;
        
        readMessagesFromBinaryStringIteratively(startOffset, binaryString, createPROTOMessage, perMessageCallback)
    }
}
/* shouldn't be invoked outside of this script */
function readByteMarkerStringIntoInt32(binaryString, startOffset, stopOffset) {
    /* first is 0x00*/
    // byte markers are signed, holding values 0-127
    var startByte = binaryString.charCodeAt(startOffset);
    startByte = startByte & 0x7f;
    if (startByte != 0){
        return undefined;
    }
    return readByteMarkerString(binaryString, startOffset + 1, stopOffset, 0, 0);
}
function readByteMarkerString(binaryString, startOffset, stopOffset, total, index) {
    if (startOffset >= stopOffset) {
        return total;
    }
    var b = binaryString.charCodeAt(startOffset);
    total += (b & 0x7f) << (index*7);
    startOffset++;
    index++;
    return readByteMarkerString(binaryString, startOffset, stopOffset, total, index);
}
/* shouldn't be invoked outside of this script */
function readMessageFromBinaryString(binaryString, startOffset, stopOffset, decodedMessage) {
    /* for now, need to convert to array */
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
