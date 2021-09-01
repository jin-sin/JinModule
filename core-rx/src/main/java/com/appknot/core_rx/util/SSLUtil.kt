package com.appknot.core_rx.util

import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 *  ssl
 *  @author Rothy on 2021/09/01
 */
object SSLUtil {
    // 유효성 검사 X
    val trustAllCerts =
        arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate?>?,
                authType: String?
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate?>?,
                authType: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                return arrayOfNulls(0)
            }
        })

    // SSL 정보 없이 인증
    fun getCertSslSocketFactory(): SSLSocketFactory? {
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        return sslContext.socketFactory
    }

    // 호스트 이름 검증 X
    internal class NullHostNameVerifier : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return true
        }
    }

    fun getSSLSafeOkHttpClient(): OkHttpClient? {
        return try {
            // Install the all-trusting trust manager
            val sslContext: SSLContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(NullHostNameVerifier())
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}