
package io.netty.example.http2.helloworld.client;

import static io.netty.handler.logging.LogLevel.INFO;

import java.net.InetSocketAddress;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import sks.samples.http2.netty.common.ClientFrameListener;

final class ClientInitializer extends ChannelInitializer<SocketChannel> {
	private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, ClientInitializer.class);
    private ClientFrameListener clientFrameListener;
	public ClientFrameListener getClientFrameListener() {
		return clientFrameListener;
	}

	public void setClientFrameListener(ClientFrameListener clientFrameListener) {
		this.clientFrameListener = clientFrameListener;
	}

	private final SslContext sslCtx;

	private HttpToHttp2ConnectionHandler connectionHandler;
	public HttpToHttp2ConnectionHandler getConnectionHandler() {
		return connectionHandler;
	}

	public void setConnectionHandler(HttpToHttp2ConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	//private HttpResponseHandler responseHandler;
	private Http2SettingsHandler settingsHandler;

	public ClientInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;

	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		final Http2Connection connection = new DefaultHttp2Connection(false);
		clientFrameListener = new  ClientFrameListener();
		connectionHandler = new HttpToHttp2ConnectionHandlerBuilder().frameListener(clientFrameListener)
				.connection(connection).build();
		//responseHandler = new HttpResponseHandler();
		settingsHandler = new Http2SettingsHandler(ch.newPromise());
		if (sslCtx != null) {
			configureSsl(ch);
		} else {
			configureClearText(ch);
		}
	}

	
	public Http2SettingsHandler settingsHandler() {
		return settingsHandler;
	}

	protected void configureEndOfPipeline(ChannelPipeline pipeline) {
		pipeline.addLast(settingsHandler);
		//pipeline.addLast(settingsHandler, responseHandler);
	}

	/**
	 * Configure the pipeline for TLS NPN negotiation to HTTP/2.
	 */
	private void configureSsl(SocketChannel ch) {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		// We must wait for the handshake to finish and the protocol to be negotiated
		// before configuring
		// the HTTP/2 components of the pipeline.
		pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
			@Override
			protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
				if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
					ChannelPipeline p = ctx.pipeline();
					p.addLast(connectionHandler);
					configureEndOfPipeline(p);
					return;
				}
				ctx.close();
				throw new IllegalStateException("unknown protocol: " + protocol);
			}
		});
	}

	/**
	 * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
	 */
	private void configureClearText(SocketChannel ch) {
		HttpClientCodec sourceCodec = new HttpClientCodec();
		Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
		HttpClientUpgradeHandler upgradeHandler = new HttpClientUpgradeHandler(sourceCodec, upgradeCodec, 65536);

		ch.pipeline().addLast(sourceCodec, upgradeHandler, new UpgradeRequestHandler(), new UserEventLogger());
	}

	/**
	 * A handler that triggers the cleartext upgrade to HTTP/2 by sending an initial
	 * HTTP request.
	 */
	private final class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			DefaultFullHttpRequest upgradeRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
					"/", Unpooled.EMPTY_BUFFER);

			// Set HOST header as the remote peer may require it.
			InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
			String hostString = remote.getHostString();
			if (hostString == null) {
				hostString = remote.getAddress().getHostAddress();
			}
			upgradeRequest.headers().set(HttpHeaderNames.HOST, hostString + ':' + remote.getPort());

			ctx.writeAndFlush(upgradeRequest);

			ctx.fireChannelActive();

			// Done with this handler, remove it from the pipeline.
			ctx.pipeline().remove(this);

			configureEndOfPipeline(ctx.pipeline());
		}
	}

	/**
	 * Class that logs any User Events triggered on this channel.
	 */
	private static class UserEventLogger extends ChannelInboundHandlerAdapter {
		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			System.out.println("User Event Triggered: " + evt);
			ctx.fireUserEventTriggered(evt);
		}
	}
}
