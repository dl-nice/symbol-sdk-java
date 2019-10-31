package io.nem.sdk.infrastructure;

import io.nem.core.crypto.PublicKey;
import io.nem.core.utils.ExceptionUtils;
import io.nem.core.utils.StringEncoder;
import io.nem.sdk.api.MetadataRepository;
import io.nem.sdk.api.RepositoryCallException;
import io.nem.sdk.api.RepositoryFactory;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.metadata.Metadata;
import io.nem.sdk.model.metadata.MetadataEntry;
import io.nem.sdk.model.metadata.MetadataType;
import io.nem.sdk.model.transaction.AccountMetadataTransactionFactory;
import io.reactivex.Observable;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MetadataTransactionServiceTest {

    private MetadataTransactionServiceImpl service;
    private MetadataRepository metadataRepositoryMock;
    private PublicAccount targetAccount;
    private PublicKey senderPublicKey;
    private NetworkType networkType = NetworkType.MIJIN_TEST;

    @BeforeEach
    void setup() {
        targetAccount = Account.generateNewAccount(networkType).getPublicAccount();
        senderPublicKey = Account.generateNewAccount(networkType).getPublicAccount().getPublicKey();
        RepositoryFactory factory = Mockito.mock(RepositoryFactory.class);
        metadataRepositoryMock = Mockito.mock(MetadataRepository.class);
        Mockito.when(factory.createMetadataRepository()).thenReturn(metadataRepositoryMock);
        service = new MetadataTransactionServiceImpl(factory);
    }

    @AfterEach
    void turnDown() {
        Mockito.verifyNoMoreInteractions(metadataRepositoryMock);
    }

    @Test
    void shouldCreateAccountMetadataTransactionFactory() throws Exception {

        BigInteger metadataKey = BigInteger.valueOf(10);
        String oldValue = "The original Message";
        String newValue = "the new Message";

        Metadata metadata = new Metadata("someId",
            new MetadataEntry("compositeHash", senderPublicKey.toHex(),
                targetAccount.getPublicKey().toHex(),
                metadataKey, MetadataType.ACCOUNT, oldValue.length(),
                oldValue, Optional.of(targetAccount.getAddress().encoded())));

        Mockito.when(
            metadataRepositoryMock
                .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                    Mockito.eq(metadataKey),
                    Mockito.eq(senderPublicKey.toHex())))
            .thenReturn(Observable.just(metadata));

        AccountMetadataTransactionFactory result =
            service.createAccountMetadataTransactionFactory(
                NetworkType.MIJIN_TEST, targetAccount,
                metadataKey,
                newValue, senderPublicKey).toFuture().get();

        Assertions.assertEquals(metadataKey, result.getScopedMetadataKey());
        Assertions.assertNotEquals(oldValue, result.getValue());
        Assertions.assertNotEquals(newValue, result.getValue());
        Assertions.assertEquals(
            StringEncoder.getBytes(newValue).length - StringEncoder.getBytes(oldValue).length,
            result.getValueSizeDelta());
        Assertions.assertEquals(targetAccount, result.getTargetAccount());

        Mockito.verify(metadataRepositoryMock)
            .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                Mockito.eq(metadataKey),
                Mockito.eq(senderPublicKey.toHex()));
    }

    @Test
    void shouldCreateAccountMetadataTransactionFactoryWhenNotFound() throws Exception {

        BigInteger metadataKey = BigInteger.valueOf(10);
        String newValue = "the new Message";

        Mockito.when(
            metadataRepositoryMock
                .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                    Mockito.eq(metadataKey),
                    Mockito.eq(senderPublicKey.toHex())))
            .thenReturn(Observable.error(new RepositoryCallException("Not Found", 404,
                null)));

        AccountMetadataTransactionFactory result =
            service.createAccountMetadataTransactionFactory(
                NetworkType.MIJIN_TEST, targetAccount,
                metadataKey,
                newValue, senderPublicKey).toFuture().get();

        Assertions.assertEquals(metadataKey, result.getScopedMetadataKey());
        Assertions.assertEquals(newValue, result.getValue());
        Assertions
            .assertEquals(StringEncoder.getBytes(newValue).length, result.getValueSizeDelta());
        Assertions.assertEquals(targetAccount, result.getTargetAccount());

        Mockito.verify(metadataRepositoryMock)
            .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                Mockito.eq(metadataKey),
                Mockito.eq(senderPublicKey.toHex()));
    }

    @Test
    void shouldNotCreateAccountMetadataTransactionFactoryWhenAnyOtherRemoteException() {

        BigInteger metadataKey = BigInteger.valueOf(10);
        String newValue = "the new Message";

        RepositoryCallException expectedException = new RepositoryCallException(
            "Some other problem.",
            500,
            null);
        Mockito.when(
            metadataRepositoryMock
                .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                    Mockito.eq(metadataKey),
                    Mockito.eq(senderPublicKey.toHex())))
            .thenReturn(Observable.error(expectedException));

        RepositoryCallException exception = Assertions
            .assertThrows(RepositoryCallException.class, () -> ExceptionUtils.propagate(() ->
                service.createAccountMetadataTransactionFactory(
                    NetworkType.MIJIN_TEST, targetAccount,
                    metadataKey,
                    newValue, senderPublicKey).toFuture().get()));

        Assertions.assertEquals(expectedException, exception);

        Mockito.verify(metadataRepositoryMock)
            .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                Mockito.eq(metadataKey),
                Mockito.eq(senderPublicKey.toHex()));
    }

    @Test
    public void shouldNotCreateAccountMetadataTransactionFactoryWhenBug() {

        BigInteger metadataKey = BigInteger.valueOf(10);
        String newValue = "the new Message";

        IllegalArgumentException expectedException = new IllegalArgumentException(
            "Some unexpected error");
        Mockito.when(
            metadataRepositoryMock
                .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                    Mockito.eq(metadataKey),
                    Mockito.eq(senderPublicKey.toHex())))
            .thenReturn(Observable.error(expectedException));

        IllegalArgumentException exception = Assertions
            .assertThrows(IllegalArgumentException.class, () -> ExceptionUtils.propagate(() ->
                service.createAccountMetadataTransactionFactory(
                    NetworkType.MIJIN_TEST, targetAccount,
                    metadataKey,
                    newValue, senderPublicKey).toFuture().get()));

        Assertions.assertEquals(expectedException, exception);

        Mockito.verify(metadataRepositoryMock)
            .getAccountMetadataByKeyAndSender(Mockito.eq(targetAccount.getAddress()),
                Mockito.eq(metadataKey),
                Mockito.eq(senderPublicKey.toHex()));
    }

}
