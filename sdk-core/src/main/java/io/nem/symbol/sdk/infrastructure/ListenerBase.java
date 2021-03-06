/*
 * Copyright 2020 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.symbol.sdk.infrastructure;

import io.nem.symbol.core.utils.MapperUtils;
import io.nem.symbol.sdk.api.Listener;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.blockchain.BlockInfo;
import io.nem.symbol.sdk.model.transaction.AggregateTransaction;
import io.nem.symbol.sdk.model.transaction.CosignatureSignedTransaction;
import io.nem.symbol.sdk.model.transaction.Deadline;
import io.nem.symbol.sdk.model.transaction.JsonHelper;
import io.nem.symbol.sdk.model.transaction.MultisigAccountModificationTransaction;
import io.nem.symbol.sdk.model.transaction.Transaction;
import io.nem.symbol.sdk.model.transaction.TransactionStatusError;
import io.nem.symbol.sdk.model.transaction.TransactionStatusException;
import io.nem.symbol.sdk.model.transaction.TransferTransaction;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.Validate;

/**
 * Created by fernando on 19/08/19.
 *
 * @author Fernando Boucquez
 */
public abstract class ListenerBase implements Listener {

    private final Subject<ListenerMessage> messageSubject = PublishSubject.create();


    private final JsonHelper jsonHelper;

    private String uid;

    protected ListenerBase(JsonHelper jsonHelper) {
        this.jsonHelper = jsonHelper;
    }

    /**
     * It knows how to handle a ws message coming from the server. Each subclass is responsible of
     * hooking the web socket implementation with this method.
     *
     * @param message the generic json with the message.
     * @param future to tell the user that the connection to the ws has been stabilised.
     */
    public void handle(Object message, CompletableFuture<Void> future) {
        if (jsonHelper.contains(message, "uid")) {
            uid = jsonHelper.getString(message, "uid");
            future.complete(null);
        } else if (jsonHelper.contains(message, "transaction")) {
            Transaction messageObject = toTransaction(message);
            ListenerChannel channel = ListenerChannel
                .rawValueOf(jsonHelper.getString(message, "meta", "channelName"));
            onNext(channel, messageObject);
        } else if (jsonHelper.contains(message, "block")) {
            BlockInfo messageObject = toBlockInfo(message);
            onNext(ListenerChannel.BLOCK, messageObject);
        } else if (jsonHelper.contains(message, "code")) {
            TransactionStatusError messageObject = new TransactionStatusError(
                MapperUtils
                    .toAddressFromEncoded(jsonHelper.getString(message, "address")),
                jsonHelper.getString(message, "hash"),
                jsonHelper.getString(message, "code"),
                new Deadline(
                    new BigInteger(jsonHelper.getString(message, "deadline"))));
            onNext(ListenerChannel.STATUS, messageObject);
        } else if (jsonHelper.contains(message, "parentHash")) {
            CosignatureSignedTransaction messageObject = toCosignatureSignedTransaction(message);
            onNext(ListenerChannel.COSIGNATURE, messageObject);
        } else if (jsonHelper.contains(message, "meta")) {
            onNext(ListenerChannel.rawValueOf(
                jsonHelper.getString(message, "meta", "channelName")),
                jsonHelper.getString(message, "meta", "hash"));
        }
    }


    @Override
    public Observable<BlockInfo> newBlock() {
        validateOpen();
        this.subscribeTo(ListenerChannel.BLOCK.toString());
        return getMessageSubject()
            .filter(rawMessage -> rawMessage.getChannel().equals(ListenerChannel.BLOCK))
            .map(rawMessage -> (BlockInfo) rawMessage.getMessage());
    }

    /**
     * Returns an observable stream of Transaction for a specific address. Each time a transaction
     * is in confirmed state an it involves the address, it emits a new Transaction in the event
     * stream.
     *
     * @param address address we listen when a transaction is in confirmed state
     * @return an observable stream of Transaction with state confirmed
     */
    @Override
    public Observable<Transaction> confirmed(final Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.CONFIRMED_ADDED.toString() + "/" + address.plain());
        return getMessageSubject()
            .filter(rawMessage -> rawMessage.getChannel().equals(ListenerChannel.CONFIRMED_ADDED))
            .map(rawMessage -> (Transaction) rawMessage.getMessage())
            .filter(transaction -> this.transactionFromAddress(transaction, address));
    }


    /**
     * Returns an observable stream of Transaction for a specific address. Each time a transaction
     * is in unconfirmed state an it involves the address, it emits a new Transaction in the event
     * stream.
     *
     * @param address address we listen when a transaction is in unconfirmed state
     * @return an observable stream of Transaction with state unconfirmed
     */
    @Override
    public Observable<Transaction> unconfirmedAdded(Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.UNCONFIRMED_ADDED + "/" + address.plain());
        return getMessageSubject()
            .filter(rawMessage -> rawMessage.getChannel().equals(ListenerChannel.UNCONFIRMED_ADDED))
            .map(rawMessage -> (Transaction) rawMessage.getMessage())
            .filter(transaction -> this.transactionFromAddress(transaction, address));
    }

    /**
     * Returns an observable stream of Transaction Hashes for specific address. Each time a
     * transaction with state unconfirmed changes its state, it emits a new message with the
     * transaction hash in the event stream.
     *
     * @param address address we listen when a transaction is removed from unconfirmed state
     * @return an observable stream of Strings with the transaction hash
     */
    @Override
    public Observable<String> unconfirmedRemoved(Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.UNCONFIRMED_REMOVED + "/" + address.plain());
        return getMessageSubject()
            .filter(
                rawMessage -> rawMessage.getChannel().equals(ListenerChannel.UNCONFIRMED_REMOVED))
            .map(rawMessage -> (String) rawMessage.getMessage());
    }

    /**
     * Return an observable of {@link AggregateTransaction} for specific address. Each time an
     * aggregate bonded transaction is announced, it emits a new {@link AggregateTransaction} in the
     * event stream.
     *
     * @param address address we listen when a transaction with missing signatures state
     * @return an observable stream of AggregateTransaction with missing signatures state
     */
    @Override
    public Observable<AggregateTransaction> aggregateBondedAdded(Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.AGGREGATE_BONDED_ADDED + "/" + address.plain());
        return getMessageSubject()
            .filter(rawMessage -> rawMessage.getChannel()
                .equals(ListenerChannel.AGGREGATE_BONDED_ADDED))
            .map(rawMessage -> (AggregateTransaction) rawMessage.getMessage())
            .filter(transaction -> this.transactionFromAddress(transaction, address));
    }

    /**
     * Returns an observable stream of Transaction Hashes for specific address. Each time an
     * aggregate bonded transaction is announced, it emits a new message with the transaction hash
     * in the event stream.
     *
     * @param address address we listen when a transaction is confirmed or rejected
     * @return an observable stream of Strings with the transaction hash
     */
    @Override
    public Observable<String> aggregateBondedRemoved(Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.AGGREGATE_BONDED_REMOVED + "/" + address.plain());
        return getMessageSubject()
            .filter(
                rawMessage -> rawMessage.getChannel()
                    .equals(ListenerChannel.AGGREGATE_BONDED_REMOVED))
            .map(rawMessage -> (String) rawMessage.getMessage());
    }

    /**
     * Returns an observable stream of {@link TransactionStatusError} for specific address. Each
     * time a transaction contains an error, it emits a new message with the transaction status
     * error in the event stream.
     *
     * @param address address we listen to be notified when some error happened
     * @return an observable stream of {@link TransactionStatusError}
     */
    @Override
    public Observable<TransactionStatusError> status(Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.STATUS + "/" + address.plain());
        return getMessageSubject()
            .filter(rawMessage -> rawMessage.getChannel().equals(ListenerChannel.STATUS))
            .map(rawMessage -> (TransactionStatusError) rawMessage.getMessage())
            .filter(status -> address.equals(status.getAddress()));
    }

    /**
     * Returns an observable stream of {@link CosignatureSignedTransaction} for specific address.
     * Each time a cosigner signs a transaction the address initialized, it emits a new message with
     * the cosignatory signed transaction in the even stream.
     *
     * @param address address we listen when a cosignatory is added to some transaction address
     * sent
     * @return an observable stream of {@link CosignatureSignedTransaction}
     */
    @Override
    public Observable<CosignatureSignedTransaction> cosignatureAdded(Address address) {
        Validate.notNull(address, "Address is required");
        validateOpen();
        this.subscribeTo(ListenerChannel.COSIGNATURE + "/" + address.plain());
        return getMessageSubject()
            .filter(rawMessage -> rawMessage.getChannel().equals(ListenerChannel.COSIGNATURE))
            .map(rawMessage -> (CosignatureSignedTransaction) rawMessage.getMessage());
    }

    private void validateOpen() {
        if (getUid() == null) {
            throw new IllegalStateException(
                "Listener has not been opened yet. Please call the open method before subscribing.");
        }
    }

    @Override
    public Observable<Transaction> confirmed(Address address, String transactionHash) {

        // I may move this method to the Listener
        Validate.notNull(address, "Address is required");
        Validate.notNull(transactionHash, "TransactionHash is required");

        Observable<Transaction> transactionListener = confirmed(address)
            .filter(t -> t.getTransactionInfo()
                .filter(
                    info -> info.getHash().filter(transactionHash::equalsIgnoreCase).isPresent())
                .isPresent());

        return getTransactionOrRaiseError(address, transactionHash, transactionListener);
    }

    @Override
    public Observable<AggregateTransaction> aggregateBondedAdded(Address address,
        String transactionHash) {
        Validate.notNull(address, "Address is required");
        Validate.notNull(transactionHash, "TransactionHash is required");

        // I may move this method to the Listener
        Observable<AggregateTransaction> transactionListener = aggregateBondedAdded(address)
            .filter(t -> t.getTransactionInfo()
                .filter(
                    info -> info.getHash().filter(transactionHash::equalsIgnoreCase).isPresent())
                .isPresent());

        return getTransactionOrRaiseError(address, transactionHash, transactionListener);
    }


    private <T extends Transaction> Observable<T> getTransactionOrRaiseError(Address address,
        String transactionHash, Observable<T> transactionListener) {

        // I may move this method to the Listener
        IllegalStateException caller = new IllegalStateException("The Caller");
        Observable<TransactionStatusError> errorListener = status(address)
            .filter(m -> transactionHash.equalsIgnoreCase(m.getHash()));
        Observable<Object> errorOrTransactionObservable = Observable
            .merge(transactionListener, errorListener).take(1);
        return errorOrTransactionObservable.map(errorOrTransaction -> {
            if (errorOrTransaction instanceof TransactionStatusError) {
                throw new TransactionStatusException(caller,
                    (TransactionStatusError) errorOrTransaction);
            } else {
                return (T) errorOrTransaction;
            }
        });
    }


    public boolean transactionFromAddress(final Transaction transaction, final Address address) {
        if (transaction.getSigner().filter(s -> s.getAddress().equals(address)).isPresent()) {
            return true;
        }

        if (transaction instanceof TransferTransaction) {
            return ((TransferTransaction) transaction).getRecipient().equals(address);
        }

        if (transaction instanceof MultisigAccountModificationTransaction) {
            return ((MultisigAccountModificationTransaction) transaction).getPublicKeyAdditions()
                .stream().anyMatch(m -> m.getAddress().equals(address));
        }
        if (transaction instanceof AggregateTransaction) {
            final AggregateTransaction aggregateTransaction = (AggregateTransaction) transaction;
            if (aggregateTransaction.getCosignatures()
                .stream().anyMatch(c -> c.getSigner().getAddress().equals(address))) {
                return true;
            }
            //Recursion...
            return aggregateTransaction.getInnerTransactions()
                .stream().anyMatch(t -> this.transactionFromAddress(t, address));
        }
        return false;
    }


    /**
     * I fires the new message object to the subject listenrs.
     *
     * @param channel the channel
     * @param messageObject the message object.
     */
    private void onNext(ListenerChannel channel, Object messageObject) {
        this.getMessageSubject().onNext(new ListenerMessage(channel, messageObject));
    }

    /**
     * Subclasses know how to map a generic blockInfoDTO json to a BlockInfo using the generated
     * DTOs of the implementation.
     *
     * @param blockInfoDTO the generic json
     * @return the model {@link BlockInfo}
     */
    protected abstract BlockInfo toBlockInfo(Object blockInfoDTO);

    /**
     * Subclasses know how to map a generic TransactionInfoDto json to a Transaction using the
     * generated DTOs of the implementation.
     *
     * @param transactionInfo the generic json
     * @return the model {@link Transaction}
     */
    protected abstract Transaction toTransaction(Object transactionInfo);

    /**
     * Subclasses know how to map a generic Consignature DTO json to a CosignatureSignedTransaction
     * using the generated DTOs of the implementation.
     *
     * @param cosignature the generic json
     * @return the model {@link CosignatureSignedTransaction}
     */
    protected abstract CosignatureSignedTransaction toCosignatureSignedTransaction(
        Object cosignature);

    protected abstract void subscribeTo(String channel);

    public Subject<ListenerMessage> getMessageSubject() {
        return messageSubject;
    }

    public JsonHelper getJsonHelper() {
        return jsonHelper;
    }

    /**
     * @return the UID connected to
     */
    @Override
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
