
package sks.samples.http2.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2EventAdapter;

public class FrameListener extends Http2EventAdapter {

  
    private ChannelHandlerContext channelHandlerContext;

   

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

   

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }
}