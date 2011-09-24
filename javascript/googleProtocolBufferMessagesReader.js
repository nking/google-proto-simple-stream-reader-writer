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
* The readMarkerAndMessages method below are for downloads into a file or blob
* when the File system API
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
* Read all gpb messages from a file or blob from the File System API.
* A function handle to create the PROTO message needs to be provided and a 
* function handle to handle the deserialized message.
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
    _readMarkerAndMessagesIteratively(fileOrBlob, 0, createPROTOMessage, perMessageCallback);
}

/**
* For browsers which do not implement FileSystem API nor BlobBuilder, but do implement Blob,
* this method accepts a signed Int8Array filled from the xmlhttprequest binary 'response'.
* A function handle to create the PROTO message needs to be provided and a 
* function handle to handle the deserialized message.
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
    _readMessagesFromInt8ArrayIteratively(0, int8Array, createPROTOMessage, perMessageCallback);
}

/**
* For browsers which do not implement FileSystem API nor BlobBuilder, nor Blob, this
* method accepts the binary string responseText from an xmlhttprequest.
* A function handle to create the PROTO message needs to be provided and a 
* function handle to handle the deserialized message.
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
    _readMessagesFromBinaryStringIteratively(0, binaryString, createPROTOMessage, perMessageCallback);
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
function _readMarkerAndMessagesIteratively(fileOrBlob, startOffset, createPROTOMessage, perMessageCallback) {

    if (startOffset >= fileOrBlob.size) {
        return;
    }

    var byteMarkerSize = 5;
    
    var byteMarkerReader = new FileReader();

    byteMarkerReader.onload = function(e) {
        var arrayBuffer = e.target.result;
        _readMessages(fileOrBlob, arrayBuffer, startOffset, byteMarkerSize, createPROTOMessage, perMessageCallback);
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
function _readMessages(fileOrBlob, arrayBuffer, startOffset, byteMarkerSize, createPROTOMessage, perMessageCallback) {
    var byteMarkerInt8Array = new Int8Array(arrayBuffer, 0, byteMarkerSize);
    
    var msgLength = _readByteMarkerIntoInt32(byteMarkerInt8Array, 0, byteMarkerSize);
    /*console.log('    eventLength:', eventLength);*/
                
    if (msgLength) {
        var msgBytesReader = new FileReader();
        var offset = startOffset + byteMarkerSize;
        var msgBlob = fileOrBlob.slice(offset, offset + msgLength);

        msgBytesReader.onload = function(e) {
            var arrayBuffer = e.target.result;
             
            var decodedmsg = createPROTOMessage();
            
            var msgArray = new Int8Array(arrayBuffer, 0, arrayBuffer.byteLength);
            _readMessageFromInt8Array(msgArray, 0, msgLength, decodedmsg);
            
            perMessageCallback(decodedmsg);
                                                
            /* start a new read */
            _readMarkerAndMessagesIteratively(fileOrBlob, offset + msgLength, createPROTOMessage, perMessageCallback);
        }
        msgBytesReader.onerror = function(e) {
            //console.log('    error while reading event bytes from fileOrBlob: ', e);
            return;
        }

        msgBytesReader.readAsArrayBuffer(msgBlob);
    }
}
function _readMessageFromInt8Array(msgInt8Array, startOffset, stopOffset, decodedMessage) {
    var array = new Array(stopOffset - startOffset);
    var i = 0;
    for (var j = startOffset; j < stopOffset; j++) {
        array[i] = msgInt8Array[j];
        i++;
    }
    var stream = new PROTO.ByteArrayStream(array);
    decodedMessage.ParseFromStream(stream);
}
function _readByteMarkerIntoInt32(markerInt8Array, startOffset, stopOffset) {
    /* first is 0x00*/
    if (markerInt8Array[startOffset] != 0) {
        return undefined;
    }
    return _readByteMarker(markerInt8Array, startOffset + 1, stopOffset, 0, 0);
}
function _readByteMarker(markerInt8Array, startOffset, stopOffset, total, index) {
    if (startOffset >= stopOffset) {
        return total;
    }
    //byte markers are signed, hold values 0-127
    var b = markerInt8Array[startOffset];
    total += (b & 0x7f) << (index*7);
    startOffset++;
    index++;
    return _readByteMarker(markerInt8Array, startOffset, stopOffset, total, index);
}
function _readMessagesFromInt8ArrayIteratively(startOffset, int8Array, createPROTOMessage, perMessageCallback) {
    //console.log("_readMessagesFromInt8ArrayIteratively");
    
    if (startOffset >= int8Array.byteLength) {
        return;
    }

    var byteMarkerSize = 5;

    // read startOffset, startOffset + byteMarkerSize from byteMarkerInt8Array
    var msgLength = _readByteMarkerIntoInt32(int8Array, startOffset, startOffset + byteMarkerSize);
    
    if (msgLength) {
        
        var decodedmsg = createPROTOMessage();
        
        startOffset += byteMarkerSize;
        var stopOffset = startOffset + msgLength;
        
        _readMessageFromInt8Array(int8Array, startOffset, stopOffset, decodedmsg);
            
        perMessageCallback(decodedmsg);
        
        startOffset+= msgLength;
        
        _readMessagesFromInt8ArrayIteratively(startOffset, int8Array, createPROTOMessage, perMessageCallback)
    }
}
function _readMessagesFromBinaryStringIteratively(startOffset, binaryString, createPROTOMessage, perMessageCallback) {
    //console.log("_readMessagesFromBinaryStringIteratively");
    
    if (startOffset >= binaryString.length) {
        return;
    }

    var byteMarkerSize = 5;

    // read startOffset, startOffset + byteMarkerSize from byteMarkerInt8Array
    var msgLength = _readByteMarkerStringIntoInt32(binaryString, startOffset, startOffset + byteMarkerSize);
    
    if (msgLength) {
        
        var decodedmsg = createPROTOMessage();
        
        startOffset += byteMarkerSize;
        var stopOffset = startOffset + msgLength;
        
        _readMessageFromBinaryString(binaryString, startOffset, stopOffset, decodedmsg);
            
        perMessageCallback(decodedmsg);
        
        startOffset+= msgLength;
        
        _readMessagesFromBinaryStringIteratively(startOffset, binaryString, createPROTOMessage, perMessageCallback)
    }
}
function _readByteMarkerStringIntoInt32(binaryString, startOffset, stopOffset) {
    /* first is 0x00*/
    // byte markers are signed, holding values 0-127
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
