
package io.netty.example.http2.helloworld.client;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import sks.samples.http2.netty.common.ClientFrameListener;
import sks.samples.http2.netty.common.SupportedPath;

public final class Client {
	static final boolean SSL = System.getProperty("ssl", "true") != null;
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
	static final String URL = System.getProperty("url", "/hello-world/api/again");
	static final String URL2 = System.getProperty("url2");
	static final String URL2DATA = System.getProperty("url2data", "test data!");
	static ClientInitializer initializer;
	static Channel channel;
	public static void main(String[] args) throws Exception {
		// Configure SSL.
		final SslContext sslCtx;
		if (SSL) {
			SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
			sslCtx = SslContextBuilder.forClient().sslProvider(provider)
					/*
					 * NOTE: the cipher filter may not include all ciphers required by the HTTP/2
					 * specification. Please refer to the HTTP/2 specification for cipher
					 * requirements.
					 */
					.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
							// NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK
							// providers.
							SelectorFailureBehavior.NO_ADVERTISE,
							// ACCEPT is currently the only mode supported by both OpenSsl and JDK
							// providers.
							SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2,
							ApplicationProtocolNames.HTTP_1_1))
					.build();
		} else {
			sslCtx = null;
		}

		EventLoopGroup workerGroup = new NioEventLoopGroup();
	    initializer = new ClientInitializer(sslCtx);

		try {
			// Configure the client.
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.remoteAddress(HOST, PORT);
			b.handler(initializer);

			// Start the client.
		    channel = b.connect().syncUninterruptibly().channel();
			System.out.println("Connected to [" + HOST + ':' + PORT + ']');

			// Wait for the HTTP/2 upgrade to occur.
			Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
			http2SettingsHandler.awaitSettings(500, TimeUnit.SECONDS);

			// HttpResponseHandler responseHandler = initializer.responseHandler();
			int streamId = 3;
			HttpScheme scheme = SSL ? HttpScheme.HTTPS : HttpScheme.HTTP;
			AsciiString hostName = new AsciiString(HOST + ':' + PORT);
			System.err.println("Sending request(s)...");
			if (URL != null) {
				// Create a simple GET request.
//	                FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, URL, Unpooled.EMPTY_BUFFER);
//	                request.headers().add(HttpHeaderNames.HOST, hostName);
//	                request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
//	                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
//	                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
//	                request.headers().add("clientId","hzhangse");
//	                request.headers().add("path", SupportedPath.PUSH_PROMISE );
//	               // responseHandler.put(streamId, channel.write(request), channel.newPromise());
//	                channel.write(request);
//	                streamId += 2;
			}
			if (URL2 != null) {
				// Create a simple POST request with a body.
//	                FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, URL2,
//	                        wrappedBuffer(URL2DATA.getBytes(CharsetUtil.UTF_8)));
//	                request.headers().add(HttpHeaderNames.HOST, hostName);
//	                request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
//	                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
//	                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
//	                channel.write(request);
				// responseHandler.put(streamId, channel.write(request), channel.newPromise());

				// sendAck(initializer, channel,0);

			}
			//sendRequestToReceiveTimerEventsAsPushPromise(initializer,channel);
			sendUndertow(initializer,channel);
			channel.flush();
			// responseHandler.awaitResponses(50, TimeUnit.SECONDS);
//	          
//	            for (int i=0;i<1;i++) {
//	              responseHandler.awaitResponses(50, TimeUnit.SECONDS);
//	            }
			// System.out.println("Finished HTTP/2 request(s)");

			// Wait until the connection is closed.
			// channel.close().syncUninterruptibly();
		} finally {
			// workerGroup.shutdownGracefully();
		}
	}

	

	public static void sendAck(int ack) throws Http2Exception {
		Http2Headers headers = getCommonHeaders();
		headers.add("ack", String.valueOf(ack));
		//headers.add(Http2Headers.PseudoHeaderName.PATH.value(), SupportedPath.PUSH_PROMISE);
		headers.add(Http2Headers.PseudoHeaderName.PATH.value(), "/hello-world/api/push");
		sendHeader(initializer, channel, headers, true);
	}

	private static void sendUndertow(ClientInitializer initializer, Channel channel)
			throws Http2Exception {

		Http2Headers headers = getCommonHeaders();
		headers.add(Http2Headers.PseudoHeaderName.PATH.value(), "/hello-world/api/push");

		sendHeader(initializer, channel, headers, true);
	}
	
	private static void sendRequestToReceiveTimerEventsAsPushPromise(ClientInitializer initializer, Channel channel)
			throws Http2Exception {

		Http2Headers headers = getCommonHeaders();
		headers.add(Http2Headers.PseudoHeaderName.PATH.value(), SupportedPath.PUSH_PROMISE);

		sendHeader(initializer, channel, headers, true);
	}

	private static void sendRequestToReceiveTimerEventsInSameStream(ClientInitializer initializer, Channel channel)
			throws Http2Exception {

		Http2Headers headers = getCommonHeaders();
		headers.add(Http2Headers.PseudoHeaderName.PATH.value(), SupportedPath.TIMER_EVENT);

		sendHeader(initializer, channel, headers, true);
	}

	private static Http2Headers getCommonHeaders() {
		// Create the header
		Http2Headers headers = new DefaultHttp2Headers();

		headers.add(Http2Headers.PseudoHeaderName.METHOD.value(), "GET");
		headers.add(Http2Headers.PseudoHeaderName.SCHEME.value(), "https");
		headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
		headers.add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
		headers.add("clientid","hzhangse");
		headers.add("batchsize","10");
		return headers;
	}

	private static void sendHeader(ClientInitializer initializer, Channel channel, Http2Headers headers,
			boolean endOfStream) throws Http2Exception {
		// Create a new stream
		ClientFrameListener listener = initializer.getClientFrameListener();
		Http2Connection connection = initializer.getConnectionHandler().connection();
		int streamId = connection.local().incrementAndGetNextStreamId();
		System.out.println("Next stream Id on the client side " + streamId);

		Http2Stream stream = connection.local().createStream(streamId, false);
		System.err.println("Created new stream - state " + stream.state() + " id " + stream.id());

		Http2ConnectionEncoder encoder = initializer.getConnectionHandler().encoder();
		ChannelHandlerContext ctx = listener.getChannelHandlerContext();
		encoder.writeHeaders(ctx, streamId, headers, 0, endOfStream, channel.newPromise());
		
//		ByteBuf RESPONSE_BYTES = unreleasableBuffer(copiedBuffer("Hello World", CharsetUtil.UTF_8));
//		
//		encoder.writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
//		encoder.writeData(ctx, streamId, RESPONSE_BYTES, 0, false, ctx.newPromise());
	}
}
