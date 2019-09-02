
package sks.samples.http2.netty.server.handler;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class TimerEventAsPushPromiseRequestHandler extends AbstractRequestHandlerBase {

	private static List<String> msgLst = new ArrayList<>();
	{
		for (int i = 0; i < 100; i++) {
			msgLst.add("message--" + i);

		}
	}
	static AtomicInteger ack = new AtomicInteger(0);

	static Map<String, Integer> checkPoint = new ConcurrentHashMap<>();

	@Override
	public void handleHeaderFrame(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
			boolean endStream, Http2ConnectionEncoder encoder) {

		System.out.println("TimerEventAsPushPromise request received on stream " + streamId);

		// send a response header first followed by data frames.
//        Http2Headers responseHeaders = new DefaultHttp2Headers();
//        responseHeaders.status(OK.codeAsText());
//        
//        encoder.writeHeaders(ctx, streamId, responseHeaders, 0, false, ctx.newPromise());
		// send data frame every 5 seconds after the initial delay of 1 second
		ctx.channel().eventLoop().scheduleAtFixedRate(
				new PushPromiseSender(ctx, streamId, encoder, getConnection(), headers), 1000, 5000,
				TimeUnit.MILLISECONDS);

	}

	public void handleAck(Http2Headers headers) {
		String clientid = headers.get("clientid").toString();
		int ack = Integer.valueOf(headers.get("ack").toString());
		checkPoint.put(clientid, ack);
	}

	@Override
	public void handleDataFrame(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding,
			boolean endOfStream) {

	}

	private static class PushPromiseSender implements Runnable {

		ChannelHandlerContext ctx;
		int streamId;
		Http2ConnectionEncoder encoder;
		Http2Connection connection;
		Http2Headers headers;

		public PushPromiseSender(ChannelHandlerContext ctx, int streamId, Http2ConnectionEncoder encoder,
				Http2Connection connection, Http2Headers headers) {
			this.ctx = ctx;
			this.streamId = streamId;
			this.encoder = encoder;
			this.connection = connection;
			this.headers = headers;
		}

		@Override
		public void run() {
			// System.out.println("Timer triggered - Thread - " +
			// Thread.currentThread().getName());
			ChannelPromise channelPromise = ctx.newPromise();
			channelPromise.addListener(new GenericFutureListener<Future<Void>>() {
				@Override
				public void operationComplete(Future<Void> future) throws Exception {
					System.out.println("Operation complete callback - " + future.isSuccess() + " Thread - "
							+ Thread.currentThread().getName());
					if (!future.isSuccess()) {
						future.cause().printStackTrace();
					}
				}
			});

			try {
				sendAPushPromise();
				encoder.flowController().writePendingBytes();
				ctx.flush();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		private void sendAPushPromise() throws Http2Exception {			
			String clientid = PushPromiseSender.this.headers.get("clientid").toString();
			int batchsize = Integer.valueOf(PushPromiseSender.this.headers.get("batchsize").toString());
			
			// Http2Stream stream =
			// connection.local().reservePushStream(pushPromiseStreamId,
			// connection.connectionStream());
			if (checkPoint.get(clientid) == null) {
				checkPoint.put(clientid, 0);
			}			

			int idx = checkPoint.get(clientid);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < batchsize; i++) {
				sb.append(msgLst.get(idx + i) + "/n");
			}

			Http2Headers headers = new DefaultHttp2Headers();
			headers.add("clientid", PushPromiseSender.this.headers.get("clientid"));
			headers.status(OK.codeAsText());
			int pushPromiseStreamId = connection.local().incrementAndGetNextStreamId();
			encoder.writePushPromise(ctx, streamId, pushPromiseStreamId, headers, 0, ctx.newPromise());
			ByteBuf payload = unreleasableBuffer(copiedBuffer("PushMessage : " + sb, CharsetUtil.UTF_8));
			encoder.writeHeaders(ctx, pushPromiseStreamId, headers, 0, false, ctx.newPromise());
			encoder.writeData(ctx, pushPromiseStreamId, payload, 0, true, ctx.newPromise());
			checkPoint.put(clientid,idx+batchsize );
		}
	}

}
