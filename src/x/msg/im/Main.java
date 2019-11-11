/*
  Copyright 2019 www.dev5.cn, Inc. dev5@qq.com
 
  This file is part of X-MSG-IM.
 
  X-MSG-IM is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  X-MSG-IM is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with X-MSG-IM.  If not, see <https://www.gnu.org/licenses/>.
 */
package x.msg.im;

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;

import misc.Crypto;
import misc.Log;
import misc.Misc;
import x.msg.pb.XmsgImAuthPb.XmsgImAuthSimpleReq;
import x.msg.pb.XmsgImAuthPb.XmsgImAuthSimpleRsp;
import x.msg.pb.XmsgXscHttpPb.XmsgImHttpRsp;
import xsc.proto.XscProto;
import xsc.proto.XscProtoPdu;
import xsc.proto.XscProtoTransaction;

/**
 * 
 * Created on: 2019年11月11日 上午9:38:03
 *
 * Author: xzwdev
 *
 */
public class Main
{
	public static final void main(String[] args) throws Exception
	{
		Main.mainTcp(args);
		Main.mainHttp(args);
		Main.mainWebSocket(args);
		Misc.hold();
	}

	public static void mainTcp(String[] args) throws Exception
	{
		XmsgImAuthSimpleReq.Builder req = XmsgImAuthSimpleReq.newBuilder();
		req.setUsr("usr");
		//
		XscProtoPdu pdu = new XscProtoPdu(); /* 基于xsc协议的pdu构造. */
		pdu.transm.indicator = 0x00;
		pdu.transm.trans = new XscProtoTransaction();
		pdu.transm.trans.trans = XscProto.XSC_TAG_TRANS_BEGIN;
		pdu.transm.trans.stid = 0x00112233;
		pdu.transm.trans.msg = XmsgImAuthSimpleReq.getDescriptor().getName();
		pdu.transm.trans.dat = req.build().toByteArray();
		//
		Socket sock = new Socket("127.0.0.1", 1224);
		sock.getOutputStream().write(pdu.bytes());
		byte by[] = new byte[0x200];
		int len = sock.getInputStream().read(by); /* 这里很不严谨, 仅用于演示. */
		pdu = XscProtoPdu.decode(by, 0, len);
		Log.info("rsp: %s", Misc.pb2str(XmsgImAuthSimpleRsp.parseFrom(pdu.transm.trans.dat)));
		sock.close();
	}

	public static void mainHttp(String[] args) throws Exception
	{
		XmsgImAuthSimpleReq.Builder req = XmsgImAuthSimpleReq.newBuilder();
		req.setUsr("usr");
		//
		HttpClient client = HttpClient.newBuilder().build();
		HttpRequest request = HttpRequest.newBuilder()//
				.uri(URI.create("http://127.0.0.1:1225/"))//
				.header("x-msg-name", XmsgImAuthSimpleReq.getDescriptor().getName())//
				.header("x-msg-dat", Crypto.base64enc(req.build().toByteArray()))//
				.build();
		HttpResponse<byte[]> rsp = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		Log.info("rsp: %s", Misc.pb2str(XmsgImAuthSimpleRsp.parseFrom(XmsgImHttpRsp.parseFrom(rsp.body()).getDat())));
	}

	public static void mainWebSocket(String[] args)
	{
		Log.setRecord();
		var httpClient = HttpClient.newHttpClient();
		var wsCompletableFuture = httpClient.newWebSocketBuilder().buildAsync(URI.create("ws://127.0.0.1:1226"), new Listener()
		{
			public void onOpen(WebSocket ws)
			{
				XmsgImAuthSimpleReq.Builder req = XmsgImAuthSimpleReq.newBuilder();
				req.setUsr("usr");
				//
				XscProtoPdu pdu = new XscProtoPdu(); /* 基于xsc协议的pdu构造. */
				pdu.transm.indicator = 0x00;
				pdu.transm.trans = new XscProtoTransaction();
				pdu.transm.trans.trans = XscProto.XSC_TAG_TRANS_BEGIN;
				pdu.transm.trans.stid = 0x00112233;
				pdu.transm.trans.msg = XmsgImAuthSimpleReq.getDescriptor().getName();
				pdu.transm.trans.dat = req.build().toByteArray();
				//
				ws.sendBinary(ByteBuffer.wrap(pdu.bytes() /* 消息出栈. */), true);
				ws.request(1);
			}

			public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer dat, boolean last)
			{
				byte by[] = new byte[dat.limit()];
				dat.get(by, 0, by.length);
				try
				{
					XscProtoPdu pdu = XscProtoPdu.decode(by, 0, by.length); /* 解析收到的响应字节流. */
					Log.info("rsp: %s", Misc.pb2str(XmsgImAuthSimpleRsp.parseFrom(pdu.transm.trans.dat)));
				} catch (Exception e)
				{
					Log.error(Log.trace(e));
				}
				ws.request(1);
				return null;
			}

			public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
			{
				Log.debug("web-socket channel closed");
				return null;
			}

			public void onError(WebSocket ws, Throwable error)
			{
				Log.debug("web-socket channel error occured: %s", Log.trace(error));
				ws.request(1);
			}
		});
		wsCompletableFuture.join();
	}
}
