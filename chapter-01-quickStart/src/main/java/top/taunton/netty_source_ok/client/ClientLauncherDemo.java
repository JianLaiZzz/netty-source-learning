/*
 *
 *  * Copyright (c) 2020 tangduns945@gmail.com.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package top.taunton.netty_source_ok.client;

import java.nio.charset.StandardCharsets;
import top.taunton.netty_source_ok.client.channelHandler.ClientSendandAcceptDataNonStickyHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
/**
 *<p>netty客户端</p>
 *@author <a href="mailto:taunton1024@gmail.com">taunton</a>
 *@version
 *@since
 *@date Created in 2020-05-21 2:21 下午
 */
public class ClientLauncherDemo {

    public static void main(String[] args) {
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<Channel>() {

                 @Override
                 protected void initChannel(Channel ch) throws Exception {
                     //存在粘包问题
                     //ch.pipeline().addLast(new ClientSendAndAcceptDataHandler());
                     //解决了粘包问题
                     ch.pipeline().addLast(new LineBasedFrameDecoder(1024*1024));
                     ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                     ch.pipeline().addLast(new ClientSendandAcceptDataNonStickyHandler());
                 }
             });
            //连接指定ip的指定端口
            //sync() 作用：阻塞主线程直到connect完成（future done）
            ChannelFuture channelFuture = b.connect("127.0.0.1", 8848).addListener(f -> {
                if(f.isSuccess()){
                    System.out.println("连接目标服务器成功！！！");
                }else {
                    System.out.println("连接目标服务器失败！！！");
                }
            }).sync();
            //TimeUnit.SECONDS.sleep(10);
            //ChannelFuture channelFuture1 = channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(("试试就试试" + System.getProperty("line.separator")).getBytes(StandardCharsets.UTF_8)));
            //channelFuture1.addListener(f -> System.out.println("试试就试试的结果 ： " + f.isDone() + " " + f.isSuccess()));
            //ChannelFuture channelFuture2 = b.connect("127.0.0.1", 8848).sync();
            //System.out.println("连接目标服务器成功2！！！");
            //sync 阻塞主线程直到channel close（future done）
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅的关闭
            group.shutdownGracefully();
        }
    }
}
