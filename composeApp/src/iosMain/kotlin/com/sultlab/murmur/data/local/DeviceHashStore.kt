package com.sultlab.murmur.data.local

import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.providers.base.toNSData
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.UIKit.UIDevice

private const val HASH_SALT     = "vs_salt_replace_with_build_constant"
private const val KEYCHAIN_KEY  = "com.voidspace.device_hash"
private const val KEYCHAIN_SERVICE = "voidspace"

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class DeviceHashStore {
    actual suspend fun getDeviceHash(): String {
        keychainRead()?.let { return it }

        val idfv = UIDevice.currentDevice.identifierForVendor?.UUIDString
            ?: NSUUID().UUIDString

        val raw = "$HASH_SALT:$idfv"
        val hash = sha256Hex(raw)

        keychainWrite(hash)
        return hash
    }

    private fun sha256Hex(input: String): String {
        val data = input.encodeToByteArray().asUByteArray()
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        CC_SHA256(data.refTo(0), data.size.toUInt(), digest.refTo(0))
        return digest.joinToString("") { it.toString(16).padStart(2, '0') }
    }

    private fun keychainRead(): String? = memScoped {
        val query = CFDictionaryCreateMutable(null, 0, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        
        val service = CFStringCreateWithCString(null, KEYCHAIN_SERVICE, kCFStringEncodingUTF8)
        val account = CFStringCreateWithCString(null, KEYCHAIN_KEY, kCFStringEncodingUTF8)
        
        CFDictionaryAddValue(query, kSecAttrService, service)
        CFDictionaryAddValue(query, kSecAttrAccount, account)
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        
        val nsData = if (status == 0) {
            result.value?.let { interpretObjCPointer<NSData>(it.rawValue) }
        } else null
        
        val finalResult = nsData?.let { 
            NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString()
        }

        if (query != null) CFRelease(query)
        if (service != null) CFRelease(service)
        if (account != null) CFRelease(account)
        
        finalResult
    }

    @OptIn(CryptographyProviderApi::class)
    private fun keychainWrite(value: String) = memScoped {
        val data = value.encodeToByteArray().toNSData()
        val query = CFDictionaryCreateMutable(null, 0, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        
        val service = CFStringCreateWithCString(null, KEYCHAIN_SERVICE, kCFStringEncodingUTF8)
        val account = CFStringCreateWithCString(null, KEYCHAIN_KEY, kCFStringEncodingUTF8)
        
        CFDictionaryAddValue(query, kSecAttrService, service)
        CFDictionaryAddValue(query, kSecAttrAccount, account)
        
        val dataRef = CFBridgingRetain(data)
        CFDictionaryAddValue(query, kSecValueData, dataRef)

        SecItemDelete(query)
        SecItemAdd(query, null)
        
        if (query != null) CFRelease(query)
        if (service != null) CFRelease(service)
        if (account != null) CFRelease(account)
        if (dataRef != null) CFRelease(dataRef)
    }
}
