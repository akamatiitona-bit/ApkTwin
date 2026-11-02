package dev.apktwin

import android.content.Context
import android.net.Uri
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.RSAKeyGenParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/**
 * Safety boundary for the repackaging pipeline.
 *
 * Important: APKTool's normal CLI is desktop-oriented. Do not invoke an arbitrary
 * downloaded JAR or shell command inside the app. A production implementation
 * must integrate and test an Android-compatible decode/rebuild engine first.
 */
object ClonePipeline {
    fun run(context: Context, source: Uri, newPackageName: String): String {
        return try {
            createOrLoadSigningKey()
            // Intentionally do not emit a misleading "success" APK. Signing a ZIP copy
            // without rewriting the compiled binary manifest/resources does not clone
            // an Android package and may create a broken or misleading artifact.
            "署名鍵の準備はできました。\n\nAPKの再構築エンジンは未統合です。\n安全にパッケージ名を変更するには、Android互換のManifest/Resource decode・rebuild処理が必要です。\n元APKは変更していません。"
        } catch (e: Exception) {
            "処理に失敗しました: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    private fun createOrLoadSigningKey() {
        val alias = "apktwin-local-signing-key"
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (store.containsAlias(alias)) return

        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(javax.security.auth.x500.X500Principal("CN=ApkTwin Local"))
            .setCertificateSerialNumber(java.math.BigInteger.valueOf(System.currentTimeMillis()))
            .setCertificateNotBefore(java.util.Date(System.currentTimeMillis() - 86400000L))
            .setCertificateNotAfter(java.util.Date(System.currentTimeMillis() + 10L * 365 * 86400000L))
            .build()
        generator.initialize(spec)
        generator.generateKeyPair()
    }
}
