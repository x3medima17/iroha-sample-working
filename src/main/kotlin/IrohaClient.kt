package iroha.getting.started

import iroha.protocol.BlockOuterClass
import iroha.protocol.Queries.Query

import iroha.protocol.QueryServiceGrpc
import iroha.protocol.CommandServiceGrpc
import iroha.protocol.Endpoint.TxStatusRequest
import iroha.protocol.Responses.QueryResponse
import iroha.protocol.Primitive.uint256


import com.google.protobuf.InvalidProtocolBufferException
import io.grpc.ManagedChannelBuilder
import com.google.protobuf.ByteString
import jp.co.soramitsu.iroha.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import mu.KLogging

import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit


class IrohaClient {
    companion object : KLogging()

    val users = listOf("admin", "alice", "bob")

    private val crypto by lazy { ModelCrypto() }
    private val txBuilder by lazy { ModelTransactionBuilder() }
    private val queryBuilder by lazy { ModelQueryBuilder() }

    private var queryCounter: Long = 1

    private val domain = "main"
    private val asset = "sora"
    private val admin = "admin"

    private val channel by lazy {
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext(true).build()
    }

    private val commandStub by lazy {
        CommandServiceGrpc.newBlockingStub(channel)
    }

    fun<T> TODO(msg: String) : T {
        throw NotImplementedError(msg)
    }

    fun getCurrentTime(): Long = System.currentTimeMillis()

    fun getKeys(user: String) = crypto.convertFromExisting(
        readKeyFromFile("resources/$user.pub"),
        readKeyFromFile("resources/$user.priv")
    )

    fun amountToLong(amount: uint256): Long {
        return (amount.first.toString(16) +
                amount.second.toString(16) +
                amount.third.toString(16) +
                amount.fourth.toString(16)).toLong(16)
    }

    fun toByteArray(blob: ByteVector): ByteArray {
        val bs = ByteArray(blob.size().toInt())
        for (i in 0 until blob.size().toInt()) {
            bs[i] = blob.get(i).toByte()
        }
        return bs
    }

    fun readKeyFromFile(path: String): String? {
        return try {
            String(Files.readAllBytes(Paths.get(path)))
        } catch (e: IOException) {
            logger.error { "Unable to read key files.\n $e" }
            null
        }
    }


    fun prepareQuery(uquery: UnsignedQuery, user: String): Query? {
        val queryBlob = ModelProtoQuery(uquery)
            .signAndAddSignature(getKeys(user))
            .finish()
            .blob()

        val bquery = toByteArray(queryBlob)

        var protoQuery: Query? = null
        try {
            protoQuery = Query.parseFrom(bquery)
        } catch (e: InvalidProtocolBufferException) {
            logger.error { "Exception while converting byte array to protobuf:" + e.message }
        }
        return protoQuery
    }

    fun isStatelessValid(resp: QueryResponse) =
        !(resp.hasErrorResponse() &&
                resp.errorResponse.reason.toString() == "STATELESS_INVALID")


    fun sendQuery(query: UnsignedQuery, creator: String): QueryResponse {
        val protoQuery = prepareQuery(query, creator)

        val queryStub = QueryServiceGrpc.newBlockingStub(channel)
        val queryResponse = queryStub.find(protoQuery)

        return TODO("Handle response if needed and return it")
    }

    fun getRoles(): List<String>? {
        return TODO("Implement getRoles query")
    }

    fun requestStatus(hash: Hash): String {
        // create status request
        logger.info { "Hash of the transaction: " + hash.hex() }

        val txhash = hash.blob()
        val bshash = toByteArray(txhash)

        val request = TxStatusRequest.newBuilder().setTxHash(ByteString.copyFrom(bshash)).build()

        val response = commandStub.status(request)
        return response.getTxStatus().name
    }

    fun sendTransaction(utx: UnsignedTx, creator: String): String? {
        // sign transaction and get its binary representation (Blob)
        val txblob = ModelProtoTransaction(utx)
            .signAndAddSignature(getKeys(creator))
            .finish()
            .blob()

        // Convert ByteVector to byte array
        val bs = toByteArray(txblob)

        // create proto object
        val protoTx: BlockOuterClass.Transaction?
        try {
            protoTx = BlockOuterClass.Transaction.parseFrom(bs)
        } catch (e: InvalidProtocolBufferException) {
            logger.error { "Exception while converting byte array to protobuf:" + e.message }
            return null
        }

        // Send transaction to iroha
        commandStub.torii(protoTx)

        // wait to ensure transaction was processed
        runBlocking {
            delay(5000, TimeUnit.MILLISECONDS)
        }

        return requestStatus(utx.hash())
    }


    fun createAsset(creator: String, name: String, precision: Short): String? {
        val utx = txBuilder.creatorAccountId(creator)
            .createdTime(BigInteger.valueOf(getCurrentTime()))
            .createAsset(name, domain, precision)
            .build()
        return sendTransaction(utx, creator)
    }

    fun getBalance(creator: String, user: String, asset: String): Long? {
        val uquery = queryBuilder.creatorAccountId(creator)
            .queryCounter(BigInteger.valueOf(queryCounter))
            .createdTime(BigInteger.valueOf(getCurrentTime()))
            .getAccountAssets(user)
            .build()

        val queryResponse = sendQuery(uquery, "$admin@$domain")

        return if (queryResponse.hasErrorResponse()) {
            logger.error { queryResponse.errorResponse.reason.toString() }
            logger.error { queryResponse.errorResponse.message }
            null
        } else {
            TODO("Get amount of given asset")
        }
    }

    fun addAssetQuantity(creator: String, asset: String, amount: Long): String? {
        val utx : UnsignedTx = TODO("Create addAssetQuantity command")

        return sendTransaction(utx, creator)
    }

    fun transferAsset(from: String, to: String, asset: String, amount: Long): String? {
        val utx : UnsignedTx = TODO("Create transferAsset command")

        return sendTransaction(utx, from)
    }

    fun createAccount(creator: String, user: String, domain: String, key: PublicKey): String? {
        val utx : UnsignedTx = TODO("Create createAccount command ")

        return TODO("Send transaction and return the result")
    }

    fun getAccountsBalances(users: List<String>, asset: String) =
        users.associate { it to getBalance("$admin@$domain", it, asset) }

    fun run() {

        createAccount("$admin@$domain", "bob", domain, getKeys("bob@$domain").publicKey())
        createAccount("$admin@$domain", "alice", domain, getKeys("alice@$domain").publicKey())
        createAsset("$admin@$domain", asset, 0)
        addAssetQuantity("$admin@$domain", "$asset#$domain", 100)
        transferAsset("$admin@$domain", "alice@$domain", "$asset#$domain", 50)

        repeat(2) {
            val aliceBalance : Long? = TODO("Call getBalance method")
            transferAsset("alice@$domain", "bob@$domain", "$asset#$domain", aliceBalance!! / 2)
        }


    }
}

