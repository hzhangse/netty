
package sks.samples.http2.netty.common;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.http2.helloworld.client.Client;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;

public class ClientFrameListener extends FrameListener {

	static AtomicInteger i = new AtomicInteger(0);
	@Override
	public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
			throws Http2Exception {

		System.out.println("ClientFrameListener.onDataRead()");
		byte[] byteArray = new byte[data.capacity()];
		data.readBytes(byteArray);
		String result = new String(byteArray);
		System.out.println(result + ":" + streamId);
		

		// Client.sendAck(6);
		return super.onDataRead(ctx, streamId, data, padding, endOfStream);
	}

	@Override
	public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
			boolean endStream) throws Http2Exception {

		System.out.println("ClientFrameListener.onHeadersRead(1)");
		System.out.println("streamId:" + streamId  + " headers:clientId="
				+ headers.get("clientid"));
		
	
		super.onHeadersRead(ctx, streamId, headers, padding, endStream);
	}

	@Override
	public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
		// save the reference to ChannelHandlerContext
		setChannelHandlerContext(ctx);
		ctx.fireChannelRead(settings);
	}

	@Override
	public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers,
			int padding) throws Http2Exception {

		System.out.println("ClientFrameListener.onPushPromiseRead()");
		System.out.println("streamId:" + streamId + " promisedStreamId:" + promisedStreamId + " headers:clientId="
				+ headers.get("clientid"));
		super.onPushPromiseRead(ctx, streamId, promisedStreamId, headers, padding);
	}

	@Override
	public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
			short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
		System.out.println("streamId:" + streamId + " streamDependency:" + streamDependency + " headers:clientId="
				+ headers.get("clientid"));
		String ack = headers.get("acked").toString();
		if (ack.equalsIgnoreCase("20"))
		  Client.sendAck(6);
		System.out.println("ClientFrameListener.onHeadersRead()");
		
	}

	@Override
	public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight,
			boolean exclusive) throws Http2Exception {
		System.out.println("ClientFrameListener.onPriorityRead()");
	}

	@Override
	public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
		System.out.println("ClientFrameListener.onRstStreamRead()");
	}

	@Override
	public void onSettingsAckRead(ChannelHandlerContext ctx) throws Http2Exception {
		System.out.println("ClientFrameListener.onSettingsAckRead()");
	}

	@Override
	public void onPingRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
		System.out.println("ClientFrameListener.onPingRead()");
	}

	@Override
	public void onPingAckRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
		System.out.println("ClientFrameListener.onPingAckRead()");
	}

	@Override
	public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData)
			throws Http2Exception {
		System.out.println("ClientFrameListener.onGoAwayRead()");
	}

	@Override
	public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement)
			throws Http2Exception {
		System.out.println("ClientFrameListener.onWindowUpdateRead()");
	}

	@Override
	public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags,
			ByteBuf payload) throws Http2Exception {
		System.out.println("ClientFrameListener.onUnknownFrame()");
	}

	@Override
	public void onStreamAdded(Http2Stream stream) {
		System.out.println("ClientFrameListener.onStreamAdded()");
	}

	@Override
	public void onStreamActive(Http2Stream stream) {
		System.out.println("ClientFrameListener.onStreamActive()");
	}

	@Override
	public void onStreamHalfClosed(Http2Stream stream) {
		System.out.println("ClientFrameListener.onStreamHalfClosed()");
	}

	@Override
	public void onStreamClosed(Http2Stream stream) {
		System.out.println("ClientFrameListener.onStreamClosed()");
	}

	@Override
	public void onStreamRemoved(Http2Stream stream) {
		System.out.println("ClientFrameListener.onStreamRemoved()");
	}

	@Override
	public void onGoAwaySent(int lastStreamId, long errorCode, ByteBuf debugData) {
		System.out.println("ClientFrameListener.onGoAwaySent()");
	}

	@Override
	public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
		System.out.println("ClientFrameListener.onGoAwayReceived()");
	}
}
