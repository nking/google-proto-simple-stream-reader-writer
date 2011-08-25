package com.google.code.proto.streamio;

import com.google.protobuf.GeneratedMessage;

/**
 * interface to handle de-serialized messages
 * 
 * @param <T> Parameterized Type inheriting from GeneratedMessage
 * 
 * @author nichole
 */
public interface IPBStreamReaderCallback<T extends GeneratedMessage> {
    
    public void handleDeserializedMessage(T message);
}
