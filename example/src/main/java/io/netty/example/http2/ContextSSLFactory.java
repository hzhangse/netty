package io.netty.example.http2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import io.netty.handler.ssl.SslHandler;

/** 
 * 初始化sslcontext类 
 * 
 */  
public class ContextSSLFactory {  
	private static final char[] STORE_PASSWORD = "sNetty".toCharArray();
    private static final SSLContext SSL_CONTEXT_S ;  
      
    private static final SSLContext SSL_CONTEXT_C ;  
      
    static{  
        SSLContext sslContext = null ;  
        SSLContext sslContext2 = null ;  
        try {  
            sslContext = SSLContext.getInstance("SSLv3") ;  
            sslContext2 = SSLContext.getInstance("SSLv3") ;  
        } catch (NoSuchAlgorithmException e1) {  
            e1.printStackTrace();  
        }  
        try{  
            if(getKeyManagersServer() != null && getTrustManagersServer() != null ){  
                sslContext.init(getKeyManagersServer(), getTrustManagersServer(), null);  
            }  
            if(getKeyManagersClient() != null && getTrustManagersClient() != null){  
                sslContext2.init(getKeyManagersClient(), getTrustManagersClient(), null);  
            }  
              
        }catch(Exception e){  
            e.printStackTrace() ;  
        }  
        sslContext.createSSLEngine().getSupportedCipherSuites() ;  
        sslContext2.createSSLEngine().getSupportedCipherSuites() ;  
        SSL_CONTEXT_S = sslContext ;   
        SSL_CONTEXT_C = sslContext2 ;  
    }  
    public ContextSSLFactory(){  
          
    }  
    public static SSLContext getSslContext(){  
        return SSL_CONTEXT_S ;  
    }  
    public static SSLContext getSslContext2(){  
        return SSL_CONTEXT_C ;  
    }  
    
    private static KeyStore loadKeyStore(String name) throws Exception {
		final InputStream stream;
		stream = ContextSSLFactory.class.getResourceAsStream(name);

		try (InputStream is = stream) {
			KeyStore loadedKeystore = KeyStore.getInstance("JKS");
			loadedKeystore.load(is, STORE_PASSWORD);
			return loadedKeystore;
		}
	}
    
    private static SSLContext createSSLContext(final KeyStore keyStore, final KeyStore trustStore) throws Exception {
		KeyManager[] keyManagers;
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, STORE_PASSWORD);
		keyManagers = keyManagerFactory.getKeyManagers();

		TrustManager[] trustManagers;
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		trustManagers = trustManagerFactory.getTrustManagers();

		SSLContext sslContext;
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, trustManagers, null);

		return sslContext;
	}
    private static TrustManager[] getTrustManagersServer(){  
        FileInputStream is = null ;  
        KeyStore ks = null ;  
        TrustManagerFactory keyFac = null ;  
          
        TrustManager[] kms = null ;  
        try {  
             // 获得KeyManagerFactory对象. 初始化位默认算法  
            keyFac = TrustManagerFactory.getInstance("SunX509") ;  
//            is =new FileInputStream( (new ClassPathResource("main/java/conf/sChat.jks")).getFile() );  
//            ks = KeyStore.getInstance("JKS") ;  
            ks = loadKeyStore("/sChat.jks");
            keyFac.init(ks) ;  
            kms = keyFac.getTrustManagers() ;  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        finally{  
            if(is != null ){  
                try {  
                    is.close() ;  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return kms ;  
    }  
    private static TrustManager[] getTrustManagersClient(){  
        FileInputStream is = null ;  
        KeyStore ks = null ;  
        TrustManagerFactory keyFac = null ;  
          
        TrustManager[] kms = null ;  
        try {  
             // 获得KeyManagerFactory对象. 初始化位默认算法  
            keyFac = TrustManagerFactory.getInstance("SunX509") ;  
            ks = loadKeyStore("/cChat.jks");
            keyFac.init(ks) ;  
            kms = keyFac.getTrustManagers() ;  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        finally{  
            if(is != null ){  
                try {  
                    is.close() ;  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return kms ;  
    }  
    private static KeyManager[] getKeyManagersServer(){  
        FileInputStream is = null ;  
        KeyStore ks = null ;  
        KeyManagerFactory keyFac = null ;  
          
        KeyManager[] kms = null ;  
        try {  
             // 获得KeyManagerFactory对象. 初始化位默认算法  
            keyFac = KeyManagerFactory.getInstance("SunX509") ;  
            ks = loadKeyStore("/sChat.jks");
            keyFac.init(ks, STORE_PASSWORD) ;  
            kms = keyFac.getKeyManagers() ;  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        finally{  
            if(is != null ){  
                try {  
                    is.close() ;  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return kms ;  
    }  
    private static KeyManager[] getKeyManagersClient(){  
        FileInputStream is = null ;  
        KeyStore ks = null ;  
        KeyManagerFactory keyFac = null ;  
          
        KeyManager[] kms = null ;  
        try {  
             // 获得KeyManagerFactory对象. 初始化位默认算法  
            keyFac = KeyManagerFactory.getInstance("SunX509") ;  
            ks = loadKeyStore("/cChat.jks");
            keyFac.init(ks, STORE_PASSWORD) ;  
            kms = keyFac.getKeyManagers() ;  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        finally{  
            if(is != null ){  
                try {  
                    is.close() ;  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return kms ;  
    }  
    
    
    private static SslHandler sslHandlerClient = null ;  
    public static SslHandler getClientSslHandler(){  
        if(sslHandlerClient == null){  
            SSLEngine sslEngine = ContextSSLFactory.getSslContext2().createSSLEngine("127.0.0.1",8992) ;  
           

            sslEngine.setUseClientMode(true) ;  
            sslHandlerClient = new SslHandler(sslEngine);  
        }  
        return sslHandlerClient ;  
    }  
    
    private static SslHandler sslHandler = null ;  
    public static SslHandler getServerSslHandler(){  
        if(sslHandler == null ){  
            SSLEngine sslEngine = ContextSSLFactory.getSslContext().createSSLEngine("127.0.0.1",8992) ;  
            sslEngine.setUseClientMode(false) ;  
            //false为单向认证，true为双向认证  
            sslEngine.setNeedClientAuth(false) ;  
            sslHandler = new SslHandler(sslEngine);  
        }  
        return sslHandler ;  
    }  
    
    public static SslHandler getServerSslHandler1(){  
        if(sslHandler == null ){  
            SSLEngine sslEngine = ContextSSLFactory.getSslContext().createSSLEngine() ;  
            sslEngine.setUseClientMode(false) ;  
            //false为单向认证，true为双向认证  
            sslEngine.setNeedClientAuth(true) ;  
            sslHandler = new SslHandler(sslEngine);  
        }  
        return sslHandler ;  
    }  
}  